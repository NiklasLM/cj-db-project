; Clojure Database GUI
;
; Technische Hochschule Mittelhessen
; Homepage: www.thm.de
; Modul: Programmieren in Clojure
;
; Diese Datei beinhaltet alle Funktionen für die Anbindung
; an die Datenbank.
;
; (C) by
; Niklas Simonis
; Dominik Eller

(ns ClojureMySQLEditor.model
  (:require [clojure.java.jdbc :as jdbc])
  (:use clojure.walk)
  (:use clojure.java.io)
  (:import main.java.DatabaseUtils)
  )

; Einbinden des Cores
(require '[ClojureMySQLEditor.view :as view])

; Java-Bibliotheken importieren
(import
  '(java.sql SQLException)
  '(com.mysql.jdbc.exceptions.jdbc4 MySQLSyntaxErrorException)
  '(com.mysql.jdbc MysqlDataTruncation)
  )

; Gibt alle Datenbanktabellen zurück
(defn get-database-tables 
  [db]
    (jdbc/with-connection db
        (into #{}
            (map #(str (% :table_name))
                (jdbc/result-set-seq (->
                    (jdbc/connection)
                    (.getMetaData)
                    (.getColumns nil nil nil "%")))))))

; Gibt alle Datenbankspalten zurück
(defn get-table-columns 
  [db, table]
  (jdbc/with-connection db
    (def rowstack (into #{}
        (map #(str (% :column_name))
             (resultset-seq (->
                   (jdbc/connection)
                   (.getMetaData)
                   (.getColumns nil nil table nil)))))))
  (into-array (reverse rowstack)))

; Gibt alle Daten einer Tabelle zurück
(defn get-table-data 
  [db, table]
  (def cols (clojure.string/join (interpose ", " (get-table-columns db table))))
  (jdbc/with-connection db
   (jdbc/with-query-results rs [(str "select " cols " from " table)]
     (def rsstack [])
     (doseq [row rs]
       (def rowstack [])
       (doseq [value row]
         (def rowstack (conj rowstack (str (val value)))))
       (def rsstack (conj rsstack (reverse rowstack))))
     (to-array-2d rsstack))))

; Gibt die Daten einer ausgewählten Zeile zurück
(defn get-table-row-data [db, table, column]
  (def cols (clojure.string/join (interpose ", " (get-table-columns db table))))
  (jdbc/with-connection db
   (jdbc/with-query-results rs [(str "select " cols " from " table)]
     (def i 0)
     (doseq [row rs]
       (if (= i column)
         [
         (def rowstack [])
         (doseq [value row]
           (def rowstack (conj rowstack (str (val value)))))
         ])
       (def i (inc i))))
   (reverse rowstack)))

; Gibt den Primärschlüssel der ausgewählten Tabelle zurück
(defn primary-keys
  [db, table]
  (jdbc/with-connection db
     (def primseq (jdbc/result-set-seq (->
                    (jdbc/connection)
                    (.getMetaData)
                    (.getPrimaryKeys nil nil table)))))
  (def primmap (first primseq))
  (val (find primmap :column_name)))

; Funktion aktualisiert die Änderungen in der Datenbank
(defn update-sqldata 
  [db, olddata, newdata, table]
  (if (.equals olddata newdata)
    ;then
    [
     (println "Nothing to do!")
     ]
    ;else
    [
     ; Holen aller Spalten
     (def tablecols (get-table-columns db table))
     (def primarykey (primary-keys db table))
     
     ; Mappen der Daten / Hinzufügen der Keys
     (def oldmap (zipmap tablecols olddata))
     (def oldmap (keywordize-keys oldmap))
     (def updatemap (zipmap tablecols newdata))
     (def updatemap (keywordize-keys updatemap))
     
     (def sqlkey (str primarykey " = ?"))
     (def sqlval (val (find oldmap (keyword primarykey))))
     
     (try
     (jdbc/with-connection db
      (jdbc/update! db (keyword table) updatemap [sqlkey sqlval]))
     (catch MysqlDataTruncation e
       (def reason (str "<HTML><BODY>Update failed!<BR>"(str (.getMessage e) "</BODY></HTML>")))
       (view/error-frame reason))
     (catch SQLException e
       (def reason (str "<HTML><BODY>Update failed!<BR>"(str (.getMessage e) "</BODY></HTML>")))
       (view/error-frame reason))
     (catch Exception e
       (def reason "Update failed!")
       (view/error-frame reason)))
     ]))

; Funktion fügt einen Eintrag in die Datenbank hinzu
(defn insert-sqldata 
  [db, newdata, table]
  ; Holen aller Spalten
  (def newtablecols (get-table-columns db table))
  (def newmap (zipmap newtablecols newdata))
  (def newmap (keywordize-keys newmap))
  
  (try
  (jdbc/with-connection db
      (jdbc/insert! db (keyword table) newmap))
  (catch MysqlDataTruncation e
    (def reason (str "<HTML><BODY>Insert failed!<BR>"(str (.getMessage e) "</BODY></HTML>")))
    (view/error-frame reason))
  (catch SQLException e
    (def reason (str "<HTML><BODY>Insert failed!<BR>"(str (.getMessage e) "</BODY></HTML>")))
    (view/error-frame reason))
  (catch Exception e
    (def reason "Insert failed!")
    (view/error-frame reason)
    )))

; Funktion zum löschen eines Eintrags
(defn delete-sqldata
  [db, data, table]
  
  (def deltablecols (get-table-columns db table))
  (def delprimarykey (primary-keys db table))
  
  (def delmap (zipmap deltablecols data))
  (def delmap (keywordize-keys delmap))
  
  (def delsqlkey (str delprimarykey " = ?"))
  (def delsqlval (val (find delmap (keyword delprimarykey))))
  
  (try
    (jdbc/with-connection db
      (jdbc/delete! db (keyword table) [delsqlkey delsqlval]))
  (catch Exception e
    (def reason "Delete failed!")
    (view/error-frame reason))))

; Funktion die das SQL Statement ausführt
(defn execute-sql-command
  [db, command]
  (if (.equals "" command)
    ;then
    [
     (println "Nothing to do!")
     ]
    ;else
    [   
     (try
       (jdbc/with-connection db
         (jdbc/query db [command]))
       (println "Execute successful")
       (catch MySQLSyntaxErrorException e
         (def reason "<HTML><BODY>Execute failed.<BR>You have an error in your SQL syntax; check the manual!</BODY></HTML>")
         (view/error-frame reason))
       (catch SQLException e
         (def reason "Execute failed. SQL-Error!")
         (view/error-frame reason))
       (catch Exception e
         (def reason "Execute failed!")
         (println e)
         (view/error-frame reason)))
     ]))
