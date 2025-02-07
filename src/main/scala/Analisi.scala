import org.apache.spark.sql.SparkSession
import org.apache.spark.rdd.RDD
import org.apache.spark.{HashPartitioner, RangePartitioner}

object Analisi extends App {

  // Verifica se è stato fornito l'argomento obbligatorio (numero di executor) al momento dell'esecuzione.
  if (args.length < 1) {
    println("Usage: Analisi <numExecutors>")
    System.exit(1)
  }

  // Legge il numero di executor passato come argomento da riga di comando e lo converte in un intero.
  val numExecutors = args(0).toInt

  // Crea una sessione Spark con un nome descrittivo per l'applicazione e avvia il contesto Spark.
  val spark = SparkSession.builder()
    .appName("CoPurchaseAnalysisOptimized")
    .getOrCreate()

  // Ottiene il contesto Spark
  val sc = spark.sparkContext

  // Registra il tempo di inizio dell'esecuzione per calcolare il tempo totale di elaborazione.
  val startTime = System.currentTimeMillis()

  try {
    println("Inizio esecuzione del job...")

    // Specifica il percorso del dataset da leggere
    val datasetPath = "gs://bucket-scp/order_products.csv"

    // Carica il file CSV come RDD di stringhe, dove ogni riga rappresenta un ordine.
    val ordersRDD: RDD[String] = sc.textFile(datasetPath)

    // Parsa ciascuna riga del file in una coppia (order_id, product_id), convertendo entrambi in interi.
    val parsedRDD: RDD[(Int, Int)] = ordersRDD.map { line =>
      val parts = line.split(",") // Suddivide la riga in base alla virgola.
      (parts(0).toInt, parts(1).toInt) // Converte i valori in interi e li restituisce come coppia.
    }

    // Calcola il numero di partizioni basato sul numero di executor, limitandolo a un massimo di 16.
    val numPartitions = math.min(numExecutors * 8, 16)

    // Raggruppa i prodotti per order_id usando reduceByKey e applica un partizionamento hash per bilanciare i dati.
    val groupedByOrderRDD: RDD[(Int, Iterable[Int])] = parsedRDD
      .mapValues(v => Iterable(v)) // Converte ogni valore in un oggetto Iterable.
      .reduceByKey(_ ++ _) // Combina i valori (prodotti) associati allo stesso order_id.
      .partitionBy(new HashPartitioner(numPartitions)) // Applica un partizionamento hash.
      .persist() // Memorizza l'RDD in memoria per migliorare le prestazioni future.

    // Genera tutte le possibili coppie di prodotti acquistati insieme nello stesso ordine.
    val productPairsRDD: RDD[(Int, Int)] = groupedByOrderRDD.flatMap { case (_, products) =>
      val productArray = products.toArray.sorted // Converte i prodotti in un array ordinato.
      productArray.combinations(2).map(pair => (pair(0), pair(1))) // Genera tutte le combinazioni di due prodotti.
    }.persist() // Memorizza l'RDD in memoria per migliorare le prestazioni future.

    // Conta quante volte ciascuna coppia di prodotti appare nei dati.
    val reducedPairsRDD = productPairsRDD
      .map(pair => (pair, 1)) // Mappa ogni coppia di prodotti con un conteggio iniziale di 1.
      .reduceByKey(_ + _) // Somma i conteggi per ciascuna coppia di prodotti.

    // Formatta i risultati come stringhe con il formato "productA,productB,count".
    val formattedResultRDD: RDD[String] = reducedPairsRDD.map { case ((productA, productB), count) =>
      s"$productA,$productB,$count"
    }.repartition(math.min(numPartitions, 16)) // Ridistribuisce i dati tra un numero limitato di partizioni.

    // Specifica il percorso di output per salvare i risultati in Google Cloud Storage.
    val outputPath = s"gs://bucket-scp/co_purchase_results_${numExecutors}worker"

    // Salva i risultati formattati come file di testo nel percorso specificato.
    formattedResultRDD.saveAsTextFile(outputPath)

    // Rilascia la memoria occupata dagli RDD persistiti per liberare risorse.
    groupedByOrderRDD.unpersist()
    productPairsRDD.unpersist()
  } catch {
    // Gestisce eventuali eccezioni durante l'esecuzione del job.
    case e: Exception =>
      println(s"Errore durante l'esecuzione: ${e.getMessage}")
      e.printStackTrace()
  } finally {
    // Calcola il tempo totale di esecuzione del job.
    val endTime = System.currentTimeMillis()
    val executionTime = (endTime - startTime) / 1000.0
    println(s"Tempo totale di esecuzione: $executionTime secondi")

    // Calcola la Strong Scaling Efficiency solo se il numero di executor è maggiore di 1.
    //In quanto l'esecuzione con un singolo nodo è calcolata sperimentalmente poi successivamente inserita
    if (numExecutors != 1) {
      val T1 = 580.579
      val speedup = T1 / (executionTime * numExecutors)
      println(f"Strong Scaling Efficiency: $speedup%.2f")
    }
    sc.stop()
  }
}