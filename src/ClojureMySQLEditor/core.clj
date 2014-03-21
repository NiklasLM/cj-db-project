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

(ns ClojureMySQLEditor.core
 (:require [clojure.java.jdbc :as jdbc])
 (:use clojure.walk)
 (:use clojure.java.io)
 (:import main.java.DatabaseUtils)
)

; Einbinden des MVC Pattern
(require '[ClojureMySQLEditor.model :as model])
(require '[ClojureMySQLEditor.controller :as controller])

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

; Globale Variablen
; Spalten
(def columns ["table"])
; Daten für Tabellenfenster
(def data [["please select a table in dropdown"]])
; Aktuell ausgewählte Tabelle
(def selectedtable "")
; JTable für den Inhalt der Tabelle
(def table (JTable. ))
; JTableModel mit Daten
(def model (proxy [DefaultTableModel]  [(to-array-2d data) (into-array columns)]))
; Lock
(def lock false)

; Aktualisieren der JTable
(defn refresh-table
  [db, seltable]
  (def lock true)
          (def columndata (model/get-table-columns db seltable))
          (def tabledata (model/get-table-data db seltable))
          (def model (proxy [DefaultTableModel] [tabledata columndata]))
          (.setModel table model)
          (def lock false))

; SQL Command Fenster, führt einen SQL Befehl auf der Datenbank aus.
(defn cmd-frame 
  [db]
  (let [
        cmdframe (JFrame. "Command:")
        
        cmd-top-panel (JPanel.)
        cmd-label (JLabel. "SQL Command:")
       
        cmd-text (JTextArea. 15 1)
        
        cmd-footer-panel (JPanel.)
        cmd-button-execute (JButton. "execute")
        cmd-button-cancel (JButton. "cancel")
       ]
    ; ActionListenere für den Execute Button
    (.addActionListener
      cmd-button-execute
      (reify ActionListener
        (actionPerformed
          [_ evt]
          ; Execute
          (def commandtext (.getText cmd-text))
          (model/execute-sql-command db commandtext)
          (.setVisible cmdframe false))))
    
    ; ActionListenere für den Cancel Button
    (.addActionListener
      cmd-button-cancel
      (reify ActionListener
        (actionPerformed
          [_ evt]
          ; Cancel
          (.setVisible cmdframe false))))
    
    (doto cmd-top-panel
      (.setBorder (EmptyBorder. 10 10 10 10))
      (.add cmd-label))
    
    (doto cmd-footer-panel
      (.setBorder (EmptyBorder. 10 10 10 10))
      (.add cmd-button-execute)
      (.add cmd-button-cancel))
    
    (doto cmdframe
	   (.add cmd-top-panel BorderLayout/PAGE_START)
     (.add cmd-text BorderLayout/CENTER)
     (.add cmd-footer-panel BorderLayout/PAGE_END)
     (.setSize 500 400)
     (.setVisible true))))

; New Entry GUI
(defn new-frame
  [db]
  (def newcols (model/get-table-columns db selectedtable))
  (def sizecols (count newcols))
  (def newdata (to-array-2d [["","","","","",""]]))

  (let [
        newframe (JFrame. "Database New Entry")
        
        top-newpanel (JLabel. "New data entry:")
        
        newtable (JTable. newdata  newcols)
        table-pane (JScrollPane. newtable)
        
        button-newframe (JPanel.)
        save-button (JButton. "save")
        cancel-button (JButton. "cancel")
       ]
    
    ; ActionListenere für den Save Button
    (.addActionListener
      save-button
      (reify ActionListener
        (actionPerformed
          [_ evt]
          ; EVENT SAVE
          (def newtable-colCount (.getColumnCount newtable))
          (def newtable-rowCount (.getRowCount newtable))
          ; Alle Werte in ein Array
          (def newtable-data [])
          (dotimes [n newtable-colCount] (def newtable-data (conj newtable-data (.getValueAt newtable 0 n))))
          ; Funktionsaufruf für InsertTable
          (model/insert-sqldata db newtable-data selectedtable)
          ; Aktualisieren der JTable
          (refresh-table db selectedtable)
          ; Ausblenden des Frames
          (.setVisible newframe false))))
    
    ; ActionListener für den Cancel Button
    (.addActionListener
      cancel-button
      (reify ActionListener
        (actionPerformed
          [_ evt]
          ; EVENT CANCEL
          (.setVisible newframe false))))
    
    ; Zusammenbauen des Button frames
    (doto button-newframe
      (.add save-button)
      (.add cancel-button))
    
    ; Zusammenbauen des Frames
    (doto newframe
      (.add top-newpanel BorderLayout/PAGE_START)
      (.add table-pane BorderLayout/CENTER)
      (.add button-newframe BorderLayout/PAGE_END)
      (.setSize 600 130)
      (.setVisible true))))

; Edit Fenster
; Zeigt den Inhalt einer Zeile
(defn edit-entry
  [db, selrow]
  
  (def rowdata (model/get-table-row-data db selectedtable selrow))
  (def olddata rowdata)
  (def editdata (to-array-2d [rowdata]))
  
  (let [
        editframe (JFrame. "Database Edit Entry")
        
        edit-toppanel (JLabel. "Edit entry:")
        
        edit-table (JTable. editdata  (model/get-table-columns db selectedtable))
        edit-pane (JScrollPane. edit-table)
        
        edit-buttonframe (JPanel.)
        edit-save-button (JButton. "save")
        edit-cancel-button (JButton. "cancel")
        edit-delete-button (JButton. "delete")
       ]
    
    ; ActionListenere für den Save Button
    (.addActionListener
      edit-save-button
      (reify ActionListener
        (actionPerformed
          [_ evt]
          ; EVENT SAVE
          (def colCount (.getColumnCount edit-table))
          (def rowCount (.getRowCount edit-table))
          ; Alle Werte in ein Array
          (def newdata [])
          (dotimes [n colCount] (def newdata (conj newdata (.getValueAt edit-table 0 n))))
          ; Funktionsaufruf für UpdateTable
          (model/update-sqldata db olddata newdata selectedtable)
          ; Aktualisieren der JTable
          (refresh-table db selectedtable)
          ; Frame ausblenden
          (.setVisible editframe false))))
    
    ; ActionListener für den Cancel Button
    (.addActionListener
     edit-cancel-button
      (reify ActionListener
        (actionPerformed
          [_ evt]
          ; EVENT CANCEL
          (.setVisible editframe false))))
    
    ; ActionListener für den Delete Button
    (.addActionListener
     edit-delete-button
      (reify ActionListener
        (actionPerformed
          [_ evt]
          ; EVENT DELETE
          (def delColCount (.getColumnCount edit-table))
          (def delRowCount (.getRowCount edit-table))
          (def deldata [])
          (dotimes [n delColCount] (def deldata (conj deldata (.getValueAt edit-table 0 n))))
          ; Funktionsaufruf zum Löschen
          (model/delete-sqldata db deldata selectedtable)
          ; Aktualisieren der JTable
          (refresh-table db selectedtable)
          (.setVisible editframe false))))
    
    ; Zusammenbauen des Button frames
    (doto edit-buttonframe
      (.add edit-save-button)
      (.add edit-cancel-button)
      (.add edit-delete-button))
    
    ; Zusammenbauen des Frames
    (doto editframe
      (.add edit-toppanel BorderLayout/PAGE_START)
      (.add edit-pane BorderLayout/CENTER)
      (.add edit-buttonframe BorderLayout/PAGE_END)
      (.setSize 600 130)
      (.setVisible true))))

; Editor GUI
; Zeigt den Inhalt einer Tabelle an
(defn editor-frame 
  [db]
  ; Tabellennamen
  (def tablenames (model/get-database-tables db))

  (let [
        frame (JFrame. "Database Table Editor")
  
        top-panel (JPanel.)
        choose-label (JLabel. "choose table:")
        choose-combo (JComboBox. (Vector. tablenames))
        
        center-table (JScrollPane. table)

        footer-panel (JPanel.)
        table-label (JLabel. "table options:")
        export-button (JButton. "export")
        import-button (JButton. "import")
        cmd-button (JButton. "cmd")
        entry-label (JLabel. "entry options:")
        insert-button (JButton. "new")
       ]
    
    ; Default Model setzen
    (.setModel table model)
    (doto table
      (.setSelectionMode ListSelectionModel/SINGLE_SELECTION))
    (-> table .getTableHeader (.setReorderingAllowed false))
    
    ; ToDo: Funktion bisher nicht unterstüzt.
    ;(.setEnabled export-button false)
    ;(.setEnabled import-button false)
    
    ; ActionListener der Dropdownbox, ändert den Inhalt der Tabelle
    (.addActionListener
      choose-combo
      (reify ActionListener
        (actionPerformed
          [_ evt]
          (def columndata (model/get-table-columns db (.getSelectedItem choose-combo)))
          (def tabledata (model/get-table-data db (.getSelectedItem choose-combo)))
          (def model (proxy [DefaultTableModel] [tabledata columndata]))
          (.setModel table model)
          (def selectedtable (.getSelectedItem choose-combo)))))
    
    ; ActionListener für New Button, fügt neue Zeile hinzu
    (.addActionListener
      insert-button
      (reify ActionListener
        (actionPerformed
          [_ evt]
          ; EVENT NEW
          ; Prüfen ob JTable mit Inhalt gefüllt ist.
          (if (.equals selectedtable "")
            [
             (println "Please select a table.")
            ]
            [
             (new-frame db)
            ]))))
    
    ; ActionListener für Export Button
    (.addActionListener
      export-button
      (reify ActionListener
        (actionPerformed
          [_ evt]
          ; EVENT EXPORT
          (controller/export-db db selectedtable)
          )))
    
    ; ActionListener für Export Button
    (.addActionListener
      import-button
      (reify ActionListener
        (actionPerformed
          [_ evt]
          ; EVENT IMPORT
          (controller/import-db)
          )))
    
    ;ActionListener für Command Button, öffnet neues Frame
    (.addActionListener
      cmd-button
      (reify ActionListener
        (actionPerformed
          [_ evt]
          ; EVENT CMD
          (cmd-frame db))))
    
    ; JTable Action Listener
    (def selmod (.getSelectionModel table))
    (.addListSelectionListener selmod
     (reify ListSelectionListener
      (valueChanged
         [_ evt]
         ; Prüfen ob ausgewählte Tabelle wirklich gleich ist
         (if (false? lock)
               [
                (if (.getValueIsAdjusting selmod)
                  [
                   (if (.equals selectedtable (.getSelectedItem choose-combo))
                     [
                      (def selrow (.getSelectedRow table))
                      ; Aufruf zum Bearbeiten in neuem Fenster
                      (edit-entry db, selrow)
                      ])
                   ])
                ]
               ))))
    
    ; Zusammenbauen des Top Panels
    (doto top-panel
      (.add choose-label)
      (.add choose-combo))
    
    ; Zusammenbauen des Footer Panels
    (doto footer-panel
      (.add table-label)
      (.add export-button)
      ;(.add import-button)
      (.add cmd-button)
      (.add entry-label)
      (.add insert-button))

    ; Zusammenbauen des Frames
    (doto frame
      (.add top-panel BorderLayout/PAGE_START)
      (.add center-table BorderLayout/CENTER)
      (.add footer-panel BorderLayout/PAGE_END)
      (.pack)
      (.setVisible true))))

; Zeigt das Connection Frame, wird direkt beim Starten des Programms ausgeführt.
(defn databaseconnect 
  []
  (let [
        login-frame (JFrame. "Database Login")
        login-panel (JPanel.)
        button-panel (JPanel.)
        top-panel (JPanel.)
        
        top-label (JLabel. "Connection")
        protcol-label (JLabel. "Protocol:")
        protcol-text (JTextField. "mysql")
        server-label (JLabel. "Server:")
        server-text (JTextField. "localhost")
        port-label (JLabel. "Server-Port:")
        port-text (JTextField. "3306")
        database-name (JLabel. "Database name: ")
        database-text (JTextField. "clojure")
        user-label (JLabel. "User:")
        user-text (JTextField. "root")
        password-label (JLabel. "Password:")
        password-text (JPasswordField. "")
        connect-button (JButton. "Connect")
        tmp-label (JLabel. "Disconnected!")
       ]
    
    ; ActionListener für Connect Button
    (.addActionListener
     connect-button
     (reify ActionListener
       (actionPerformed
         [_ evt]
         (let [
               db-host (str (.getText server-text))
               db-port (.getText port-text)
               db-name (str (.getText database-text))
              ]
           ; Definieren der Datenbankverbindung
           ; ToDo: Treiber anpassen, Fenster für Protokoll noch deaktiviert
           (def db {:classname "com.mysql.jdbc.Driver"
                    :subprotocol (str (.getText protcol-text))
                    :subname (str "//"(.getText server-text)":"(.getText port-text)"/"(.getText database-text))
                    :user (str (.getText user-text))
                    :password (str (.getText password-text))}))
         
         ; Aufbauen der Verbindung mit Exceptionhandling
         (try
           (jdbc/get-connection db)
           (.setForeground tmp-label (. Color green))
           (.setText tmp-label "Connected!")
           (println "Verbindung erfolgreich hergestellt!")
           (doto login-frame (.setVisible false))
           (editor-frame db)
           ; Bei Fehler
           (catch Exception e
             (println "Verbindung fehlgeschlagen!")
             (.setForeground tmp-label (. Color red))
             (.setText tmp-label "Disconnected!"))))))
    
    ; Label Farbe setzen
    (doto tmp-label
      (.setForeground (. Color red)))
    
    ; Disable Protokollfeld
    (doto protcol-text
      (.setEnabled false))
    
    ; Zusammenbauen des Top-Panels
    (doto top-panel
      (.setBorder (EmptyBorder. 10 10 10 10))
      (.add tmp-label))
    
    ; Zusammenbauen des Login Panels
    (doto login-panel
      (.setBorder (EmptyBorder. 10 10 10 10))
      (.setLayout (GridLayout. 6 2))
      (.add protcol-label)
      (.add protcol-text)
      (.add server-label)
      (.add server-text)
      (.add port-label)
      (.add port-text)
      (.add database-name)
      (.add database-text)
      (.add user-label)
      (.add user-text)
      (.add password-label)
      (.add password-text))
    
    ; Zusammenbauen des Button Panels
    (doto button-panel
      (.setBorder (EmptyBorder. 10 10 10 10))
      (.add connect-button))

    ; Zusammenbauen des Frames
    (doto login-frame
      (.add top-panel BorderLayout/PAGE_START)
      (.add login-panel BorderLayout/CENTER)
      (.add button-panel BorderLayout/PAGE_END)
      (.pack)
      (.setVisible true))))

; Starten der Anwendung
(databaseconnect)