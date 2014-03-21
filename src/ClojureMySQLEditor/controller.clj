; Clojure Database GUI
;
; Technische Hochschule Mittelhessen
; Homepage: www.thm.de
; Modul: Programmieren in Clojure
;
; Diese Anwendung verbindet sich mit einer MySQL-Datenbank und stellt den Inhalt dar.
; Zusätzlich können Funktionen wie Bearbeiten, Hinzufügen, Löschen, Kommandozeile und Exportieren
; der Datenbank ausgeführt werden.
;
; (C) by
; Niklas Simonis
; Dominik Eller

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

; Export die ausgewählte Tabelle in eine Datei
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


; Import Database
; Bisher nicht vorhanden.
(defn import-db
  [ ]
  )