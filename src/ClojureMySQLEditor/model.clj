; ClojureMySQL Editor
;
; Technische Hochschule Mittelhessen
; Homepage: http://www.mni.thm.de
; Modul: Programmieren in Clojure
;
; Dieses Programm verbindet sich mit einer MySQL-Datenbank und stellt den Inhalt grafisch dar.
; Zusätzlich kann der Anwender Funktionen wie Bearbeiten, Hinzufügen, Löschen, Kommandozeile und Exportieren
; auf der Datenbank ausgeführt werden.
;
; @version     1.0.0
; @package     ClojureMySQLEditor
; @name        ClojureMySQLEditor.model
; @author      Niklas Simonis
; @author      Dominik Eller
; @description Diese Datei enthält Funktionen die Daten von der Datenbank holen und schreiben.
; @link        https://github.com/NiklasLM/clj-db-project

(ns ClojureMySQLEditor.model
  (:require [clojure.java.jdbc :as jdbc])
  (:use clojure.walk)
  (:use clojure.java.io)
  (:import main.java.DatabaseUtils)
  )

; Einbinden des Cores
(require '[ClojureMySQLEditor.view :as view])

; Java-Klassen importieren
(import
  '(java.sql SQLException)
  '(com.mysql.jdbc.exceptions.jdbc4 MySQLSyntaxErrorException)
  '(com.mysql.jdbc MysqlDataTruncation)
  )

; @name get-database-tables
; @description Gibt alle Tabellen einer Datenbank zurück
; @param - db - Datenbankverbindung
; @return - Array - Alle Tabellen
(defn get-database-tables 
  [db]
    (jdbc/with-connection db
        (into #{}
            (map #(str (% :table_name))
                (jdbc/result-set-seq (->
                    (jdbc/connection)
                    (.getMetaData)
                    (.getColumns nil nil nil "%")))))))

; @name get-table-columns
; @description Gibt alle Spalten einer Tabelle zurück
; @param - db - Datenbankverbindung
; @param - table - Ausgewählte Tabelle
; @return - Array - Alle Spalten einer Tabelle
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

; @name get-table-data
; @description Gibt alle Daten einer Tabelle zurück
; @param - db - Datenbankverbindung
; @param - table - Ausgewählte Tabelle
; @return - Array - Inhalt einer Tabelle
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

; @name get-table-row-data
; @description  Gibt die Daten einer ausgewählten Zeile zurück
; @param - db - Datenbankverbindung
; @param - table - Ausgewählte Tabelle
; @param - column - Ausgewählte Spalte
; @return - Array - Inhalt einer Zeile einer Tabelle
(defn get-table-row-data 
  [db, table, column]
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

; @name primary-keys
; @description Gibt den Primärschlüssel der ausgewählten Tabelle zurück
; @param - db - Datenbankverbindung
; @param - table - Ausgewählte Tabelle
; @return - Array - Enthält PrimaryKey einer Tabelle
(defn primary-keys
  [db, table]
  (jdbc/with-connection db
     (def primseq (jdbc/result-set-seq (->
                    (jdbc/connection)
                    (.getMetaData)
                    (.getPrimaryKeys nil nil table)))))
  (def primmap (first primseq))
  (val (find primmap :column_name)))

; @name update-sqldata
; @description Funktion aktualisiert die Änderungen in der Datenbank, bei Fehler wird ein Error-Frame erzeugt.
; @param - db - Datenbankverbindung
; @param - olddata - Alten Daten eines Eintrags
; @param - newdata - Neue Datein eines Eintrags
; @param - table - Ausgewählte Tabelle
; @return void
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

; @name insert-sqldata
; @description Funktion fügt einen Eintrag in die Datenbank hinzu, bei Fehler wird ein Error-Frame erzeugt.
; @param - db - Datenbankverbindung
; @param - newdata - Neue Datein eines Eintrags
; @param - table - Ausgewählte Tabelle
; @return void
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

; @name delete-sqldata
; @description Funktion zum löschen eines Eintrags, bei Fehler wird ein Error-Frame erzeugt.
; @param - db - Datenbankverbindung
; @param - data - Enthält die Daten des Eintrags
; @param - table - Ausgewählte Tabelle
; @return void
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

; @name execute-sql-command
; @description Funktion die das SQL Statement ausführt, bei Fehler wird ein Error-Frame erzeugt.
; @param - db - Datenbankverbindung
; @param - command - Enthält das auszuführende Kommando
; @return void
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
