(ns ClojureMySQLEditor.core
  (:use clojure.java.jdbc))

; Java-Bibliotheken importieren
(import
  '(javax.swing JFrame JLabel JTextField JButton JComboBox JTable JPanel JScrollPane JPasswordField)
  '(javax.swing.table DefaultTableModel TableCellRenderer)
  '(javax.swing.event TableModelListener ListSelectionListener)
  '(java.awt.event ActionListener ItemListener)
  '(javax.swing.border EmptyBorder)
  '(java.util Vector)
  '(java.awt GridLayout Color GridBagLayout BorderLayout ComponentOrientation Dimension)
  '(java.sql.*))

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

; Datenbanktabellen
(defn get-database-tables [db]
    (with-connection db
        (into #{}
            (map #(str (% :table_name))
                (result-set-seq (->
                    (connection)
                    (.getMetaData)
                    (.getColumns nil nil nil "%")))))))

; Datenbanktabellen Spalten
(defn get-table-columns [db, table]
  (with-connection db
    (def rowstack (into #{}
        (map #(str (% :column_name))
             (resultset-seq (->
                   (connection)
                   (.getMetaData)
                   (.getColumns nil nil table nil)))))))
  (into-array (reverse rowstack)))

; Datenbanktabellen Daten
(defn get-table-data [db, table]
  (def cols (clojure.string/join (interpose ", " (get-table-columns db table))))
  (with-connection db
   (with-query-results rs [(str "select " cols " from " table)]
     (def rsstack [])
     (doseq [row rs]
       (def rowstack [])
       (doseq [value row]
         (def rowstack (conj rowstack (str (val value)))))
       (def rsstack (conj rsstack (reverse rowstack))))
     (to-array-2d rsstack))))

; CMD GUI
(defn cmd-frame [db]
  (let [cmdframe (JFrame. "Command:")
        cmdlabel (JLabel. "SQL Command:")
        cmdtext (JTextField.)
        cmdexecute (JButton. "execute")]

    (doto cmdframe
	   (.setLayout (GridLayout. 3 1))
	   (.add cmdlabel)
     (.add cmdtext)
     (.add cmdexecute)
	   (.setVisible true)
	   (.pack))))

; New Entry GUI
(defn new-frame
  [db]
  (def newcols (get-table-columns db selectedtable))
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
      (.pack)
      (.setVisible true))))

; Editor GUI
; Zeigt den Inhalt einer Tabelle an
(defn editor-frame 
  [db]
  ; Tabellennamen
  (def tablenames (get-database-tables db))

  (let [
        frame (JFrame. "Database Table Editor")
  
        top-panel (JPanel.)
        choose-label (JLabel. "choose table:")
        choose-combo (JComboBox. (Vector. tablenames))
        
        center-panel (JScrollPane. table)
        
        footer-panel (JPanel.)
        table-label (JLabel. "table options:")
        export-button (JButton. "export")
        cmd-button (JButton. "cmd")
        entry-label (JLabel. "entry options:")
        delete-button (JButton. "delete")
        insert-button (JButton. "new")
       ]
    
    ; ActionListener der Dropdownbox, ändert den Inhalt der Tabelle
    (.addActionListener
      choose-combo
      (reify ActionListener
        (actionPerformed
          [_ evt]
          (def columndata (get-table-columns db (.getSelectedItem choose-combo)))
          (def tabledata (get-table-data db (.getSelectedItem choose-combo)))
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
          (new-frame db))))
    
    ; ActionListener für Delete Button, löscht die Ausgewählte Zeile
    (.addActionListener
      delete-button
      (reify ActionListener
        (actionPerformed
          [_ evt]
          ; EVENT DELETE
          )))
    
    ; ActionListener für Export Button
    (.addActionListener
      export-button
      (reify ActionListener
        (actionPerformed
          [_ evt]
          ; EVENT EXPORT
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
    (.addListSelectionListener (.getSelectionModel table)
     (reify ListSelectionListener
      (valueChanged
         [_ evt]
         ; Pürfen ob ausgewählte Tabelle wirklich gleich ist
         (if (.equals selectedtable (.getSelectedItem choose-combo))
           [
            (def selrow (.getSelectedRow table))
            (def seldata (.getValueAt table selrow 1))
            (println "Update data!")
            ]))))
    
    ; Zusammenbauen des Top Panels
    (doto top-panel
      (.add choose-label)
      (.add choose-combo))
    
    ; Zusammenbauen des Footer Panels
    (doto footer-panel
      (.add table-label)
      (.add export-button)
      (.add cmd-button)
      (.add entry-label)
      (.add delete-button)
      (.add insert-button))

    ; Zusammenbauen des Frames
    (doto frame
      (.add top-panel BorderLayout/PAGE_START)
      (.add center-panel BorderLayout/CENTER)
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
           (get-connection db)
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
