(ns ClojureMySQLEditor.core)

(use 'clojure.java.jdbc)

(import '(javax.swing JFrame JLabel JTextField JButton JComboBox JTable JPanel JScrollPane)
        '(java.awt.event ActionListener ItemListener)
        '(java.util Vector)
        '(java.awt GridLayout Color GridBagLayout BorderLayout ComponentOrientation Dimension)
        '(java.sql.*))

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
  )

; Datenbanktabellen Daten
(defn get-table-data [db, table]
  
  (def data [])
  (with-connection db
  (transaction
   (with-query-results rs [(str "select * from " table)] 
     ; rs will be a sequence of maps, 
     ; one for each record in the result set. 
     (doseq [row rs]
       (doseq [value row]
         (concat rowdata (str (val value)))))))))


; Editor GUI
(defn editor-frame [db]
  (def tablenames (get-database-tables db))

  (def columns ["Book" "Author"])
  (def data [["On Lisp" "Paul Graham"]
           ["Practical Common Lisp" "Peter Seibel"]
           ["Programming Clojure" "Stuart Holloway"]])
  
  (let [
        frame (JFrame. "Database Table Editor")
        pane (.getContentPane frame) ]
    (let [
          top-panel (let [choose-frame (JPanel.)
                          choose-label (JLabel. "choose table:")
                          choose-combo (JComboBox. (Vector. tablenames))]
                      (.addActionListener
                        choose-combo
                        (reify ActionListener
                          (actionPerformed
                            [_ evt]
                            (def columndata (get-table-columns db (.getSelectedItem choose-combo)))
                            (def tabledata (get-table-data db (.getSelectedItem choose-combo)))
                      )))
                             
                      (doto choose-frame
                        (.add choose-label)
                        (.add choose-combo)))
          
          center-panel (JScrollPane. 
                         (JTable. (to-array-2d data)  (into-array columns)))
          
          footer-panel (let [button-frame (JPanel.)
                          table-label (JLabel. "table options:")
                          export-button (JButton. "export")
                          entry-label (JLabel. "entry options:")
                          delete-button (JButton. "delete")
                          insert-button (JButton. "new")
                          ]
                         (doto button-frame
                           (.add table-label)
                           (.add export-button)
                           (.add entry-label)
                           (.add delete-button)
                           (.add insert-button)))]
    (do
      (.setComponentOrientation pane ComponentOrientation/RIGHT_TO_LEFT)
        (doto pane
          (.add top-panel BorderLayout/PAGE_START)
          (.add center-panel BorderLayout/CENTER)
          (.add footer-panel BorderLayout/PAGE_END))))
    (.pack frame)
    (.setVisible frame true)))

(defn databaseconnect []
  ; CONNECTION HANDLING
  (let [login-frame (JFrame. "Database Login")
       protcol-label (JLabel. "protocol:")
       protcol-text (JTextField. "mysql")
       server-label (JLabel. "server:")
       server-text (JTextField. "localhost")
       port-label (JLabel. "server-port:")
       port-text (JTextField. "3306")
       database-name (JLabel. "database name:")
       database-text (JTextField. "clojure")
       user-label (JLabel. "user:")
       user-text (JTextField. "root")
       password-label (JLabel. "password:")
       password-text (JTextField. "")
       connect-button (JButton. "Connect")
       tmp-label (JLabel. "status: disconnected!")]
    (.addActionListener
     connect-button
     (reify ActionListener
            (actionPerformed
             [_ evt]
             (let [db-host "localhost"
                   db-port 3306
                   db-name "clojure"]

               (def db {:classname "com.mysql.jdbc.Driver"
                        :subprotocol (str (.getText protcol-text))
                        :subname (str "//"(.getText server-text)":"(.getText port-text)"/"(.getText database-text))
                        :user (str (.getText user-text))
                        :password (str (.getText password-text))}))
             (try
               (get-connection db)
               (.setForeground tmp-label (. Color green))
               (.setText tmp-label "status: connected!")
               (println "Verbindung erfolgreich hergestellt!")
               (doto login-frame (.setVisible false))
               (editor-frame db)

               (catch Exception e
                 (println "Verbindung fehlgeschlagen!")
                 (.setForeground tmp-label (. Color red))
                 (.setText tmp-label "status: disconnected!"))))))

    (doto login-frame
      (.setLayout (GridLayout. 7 2))
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
      (.add password-text)
      (.add connect-button)
      (.add tmp-label)
      (.setSize 300 250)
      (.setVisible true))))

(databaseconnect)

