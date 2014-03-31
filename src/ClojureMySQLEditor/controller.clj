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
; @name        ClojureMySQLEditor.controller
; @author      Niklas Simonis
; @author      Dominik Eller
; @description Diese Datei enthält Funktionen die vom Core verwendet werden.
; @link        https://github.com/NiklasLM/clj-db-project

(ns ClojureMySQLEditor.controller
  (:require [clojure.java.jdbc :as jdbc])
  (:use clojure.walk)
  (:use clojure.java.io)
  (:import main.java.DatabaseUtils)
  )

; Java-Bibliotheken importieren
(import
  '(javax.swing ListSelectionModel JFileChooser DefaultCellEditor JFrame JLabel JTextField JButton JComboBox JTable JPanel JScrollPane JPasswordField JTextArea)
  '(javax.swing.table DefaultTableModel TableCellRenderer)
  '(javax.swing.event TableModelListener ListSelectionListener)
  '(java.awt.event ActionListener ItemListener)
  '(javax.swing.filechooser FileNameExtensionFilter)
  '(javax.swing.border EmptyBorder)
  '(java.util Vector)
  '(java.awt GridLayout Color GridBagLayout BorderLayout ComponentOrientation Dimension)
  '(java.sql SQLException)
  '(com.mysql.jdbc.exceptions.jdbc4 MySQLSyntaxErrorException)
  )

; @name export-db
; @description Exportiert eine ausgewählte Tabelle und speichert diese lokal ab.
; @param - db - Datenbankverbindung
; @param - table - String - Die ausgewählte Tabelle
; @return void
(defn export-db 
  [db, table]
  (let [
        extFilter (FileNameExtensionFilter. "SQL (.sql)" (into-array  ["sql"]))
        filechooser (JFileChooser. (System/getProperty "user.home"))
        dummy (.setFileFilter filechooser extFilter)
        retval (.showSaveDialog filechooser nil)
       ]
    
    (if (= retval JFileChooser/APPROVE_OPTION)
      ;then
      [
       (def filename (.getSelectedFile filechooser))
       (if (.endsWith (str filename) ".sql")
         [
          ;Nothing yet
          ]
         [
          (def filename (str filename ".sql"))
          ]
         )
      
       ; Connection Details laden
       (def subprotocol (val (find db :subprotocol)))
       (def subname (val (find db :subname)))
       (def user (val (find db :user)))
       (def password (val (find db :password)))
       
       ; Daten holen
       (def xyz (main.java.DatabaseUtils/getExport table (str subprotocol) (str subname) (str user) (str password)))
       
       ; In Datei schreiben
       (with-open [wrtr (writer filename)]
         (.write wrtr xyz))
       ]
      ;else
      [
       ]
      )))