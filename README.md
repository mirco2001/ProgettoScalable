# **Progetto Scalable**  
**Scalable and Cloud Programming 2024/2025**  

## **Preparazione del progetto**  
Prima di eseguire i comandi sulla Google Cloud Shell, è necessario scaricare il progetto e utilizzare la shell SBT integrata in IntelliJ.  

1. Aprire la shell SBT in IntelliJ.  
2. Eseguire il comando:  
   ```
   compile
   ```  
   per compilare il progetto e generare il file `.jar`.  

Il file `.jar` verrà creato all'interno della cartella del progetto, nel seguente percorso:  
**`target/scala-<versione>/scalableproject_2.12-0.1.0-SNAPSHOT.jar`**  

---

## **Esecuzione dei comandi su Google Cloud Shell**  

### **1. Creazione del bucket su Google Cloud Storage**  
```
gcloud storage buckets create gs://bucket-scp --location=us-central1
```
📌 *Questo comando crea un bucket in Google Cloud Storage per archiviare i file necessari al progetto.*  

### **2. Caricamento dei file nel bucket**  
- **Caricamento del file JAR (Job da eseguire sul cluster):**  
  ```
  gcloud storage cp Pathfile/scalableproject_2.12-0.1.0-SNAPSHOT.jar gs://bucket-scp/
  ```
- **Caricamento del dataset:**  
  ```
  gcloud storage cp PathFile/order_products.csv gs://bucket-scp/
  ```
  
---

## **Configurazione e avvio del cluster**  

### **Cluster con 1 Worker**  
- Creazione del cluster con 1 nodo:  
  ```
  gcloud dataproc clusters create my-cluster --region=us-central1 --single-node --master-machine-type=n1-standard-8 --master-boot-disk-size=240GB --properties=spark:spark.executor.memory=12g,spark:spark.driver.memory=12g,spark:spark.shuffle.memoryFraction=0.6
  ```
- Avvio del job sul cluster:  
  ```
  gcloud dataproc jobs submit spark --cluster=my-cluster --region=us-central1 --jar=gs://bucket-scp/jobs/scalableproject_2.12-0.1.0-SNAPSHOT.jar -- 1
  ```
- Eliminazione del cluster:  
  ```
  gcloud dataproc clusters delete my-cluster --region=us-central1 
  ```
- Creazione singolo file di output in formato csv:
  ```
  gsutil cat gs://bucket-scp/co_purchase_results_1worker/part-* | gsutil cp - gs://bucket-scp/co_purchase_results_1worker/combined_output.csv
  gsutil rm gs://bucket-scp/co_purchase_results_1worker/part-*  
  ``` 

### **Cluster con 2 Worker**  
- Creazione del cluster con 2 nodi:  
  ```
  gcloud dataproc clusters create my-cluster --region=us-central1 --num-workers=2 --master-machine-type=n1-standard-4 --worker-machine-type=n1-standard-4 --master-boot-disk-size=240GB --worker-boot-disk-size=240GB --properties=spark:spark.executor.memory=8g,spark:spark.driver.memory=8g,spark:spark.shuffle.memoryFraction=0.6,spark:spark.io.compression.codec=lz4
  ```
- Avvio del job sul cluster:  
  ```
  gcloud dataproc jobs submit spark --cluster=my-cluster --region=us-central1 --jar=gs://bucket-scp/jobs/scalableproject_2.12-0.1.0-SNAPSHOT.jar -- 2
  ```
- Eliminazione del cluster:  
  ```
  gcloud dataproc clusters delete my-cluster --region=us-central1 
  ```
- Creazione singolo file di output in formato csv:
  ```
  gsutil cat gs://bucket-scp/co_purchase_results_2worker/part-* | gsutil cp - gs://bucket-scp/co_purchase_results_2worker/combined_output.csv
  gsutil rm gs://bucket-scp/co_purchase_results_2worker/part-*  
  ``` 

### **Cluster con 3 Worker**  
- Creazione del cluster con 3 nodi:  
  ```
  gcloud dataproc clusters create my-cluster --region=us-central1 --num-workers=3 --master-machine-type=n1-standard-4 --worker-machine-type=n1-standard-4 --master-boot-disk-size=240GB --worker-boot-disk-size=240GB --properties=spark:spark.executor.memory=8g,spark:spark.driver.memory=8g,spark:spark.shuffle.memoryFraction=0.6,spark:spark.io.compression.codec=lz4
  ```
- Avvio del job sul cluster:  
  ```
  gcloud dataproc jobs submit spark --cluster=my-cluster --region=us-central1 --jar=gs://bucket-scp/jobs/scalableproject_2.12-0.1.0-SNAPSHOT.jar -- 3
  ```
- Eliminazione del cluster:  
  ```
  gcloud dataproc clusters delete my-cluster --region=us-central1 
  ```
- Creazione singolo file di output in formato csv:
  ```
  gsutil cat gs://bucket-scp/co_purchase_results_3worker/part-* | gsutil cp - gs://bucket-scp/co_purchase_results_3worker/combined_output.csv
  gsutil rm gs://bucket-scp/co_purchase_results_3worker/part-*  
  ``` 

### **Cluster con 4 Worker**  
- Creazione del cluster con 4 nodi:  
  ```
  gcloud dataproc clusters create my-cluster --region=us-central1 --num-workers=4 --master-machine-type=n1-standard-4 --worker-machine-type=n1-standard-4 --master-boot-disk-size=240GB --worker-boot-disk-size=240GB --properties=spark:spark.executor.memory=8g,spark:spark.driver.memory=8g,spark:spark.shuffle.memoryFraction=0.6,spark:spark.io.compression.codec=lz4
  ```
- Avvio del job sul cluster:  
  ```
  gcloud dataproc jobs submit spark --cluster=my-cluster --region=us-central1 --jar=gs://bucket-scp/jobs/scalableproject_2.12-0.1.0-SNAPSHOT.jar -- 4
  ```
- Eliminazione del cluster:  
  ```
  gcloud dataproc clusters delete my-cluster --region=us-central1 
  ```
- Creazione singolo file di output in formato csv:
  ```
  gsutil cat gs://bucket-scp/co_purchase_results_4worker/part-* | gsutil cp - gs://bucket-scp/co_purchase_results_4worker/combined_output.csv
  gsutil rm gs://bucket-scp/co_purchase_results_4worker/part-*  
  ``` 
---

📌 **Nota:**  
- Sostituisci `Pathfile/` con il percorso corretto dei file locali.   
