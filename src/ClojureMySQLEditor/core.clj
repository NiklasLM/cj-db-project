(ns ClojureMySQLEditor.core
  (:use clojure.java.jdbc))

(import
  '(javax.swing JFrame JLabel JTextField JButton JComboBox JTable JPanel JScrollPane)
  '(javax.swing.table DefaultTableModel TableCellRenderer)
  '(javax.swing.event TableModelListener)
  '(java.awt.event ActionListener ItemListener)
  '(java.util Vector)
  '(java.awt GridLayout Color GridBagLayout BorderLayout ComponentOrientation Dimension)
  '(java.sql.*))

; DEFS
(def columns ["table"])
(def data [["please select an table in dropdown"]])
(def selectedtable "")
(def table (JTable. ))
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
(defn new-frame [db]
  (def newcols (get-table-columns db selectedtable))
  (def sizecols (count newcols))

  (def newdata (to-array-2d [["","","","","",""]]))

  (let [newframe (JFrame. "Database New Entry")
        newpane (.getContentPane newframe) ]
    (let [top-newpanel (JLabel. "New data entry:")
          newtable (JTable. newdata  newcols)
          table-newentry (JScrollPane. newtable)
          footer-newpanel (let [button-newframe (JPanel.)
                                save-button (JButton. "save")
                                cancel-button (JButton. "cancel")]

                         (.addActionListener
                           save-button
                           (reify ActionListener
                             (actionPerformed
                               [_ evt]
                               ; EVENT SAVE
                               (.setVisible newframe false)
                               )))
                         (.addActionListener
                           cancel-button
                           (reify ActionListener
                             (actionPerformed
                               [_ evt]
                               ; EVENT CANCEL
                               (.setVisible newframe false)
                                  )))

                           (doto button-newframe
                             (.add save-button)
                             (.add cancel-button)))]
    (do
      (.setComponentOrientation newpane ComponentOrientation/RIGHT_TO_LEFT)
        (doto newpane
          (.add top-newpanel BorderLayout/PAGE_START)
          (.add table-newentry BorderLayout/CENTER)
          (.add footer-newpanel BorderLayout/PAGE_END)
          )))
    (.pack newframe)
    (.revalidate newframe)
    (.setVisible newframe true)))

; Editor GUI
(defn editor-frame [db]
  (def tablenames (get-database-tables db))

  (let [frame (JFrame. "Database Table Editor")
        pane (.getContentPane frame) ]
    (let [top-panel (let [choose-frame (JPanel.)
                          choose-label (JLabel. "choose table:")
                          choose-combo (JComboBox. (Vector. tablenames))]
                      (.addActionListener
                        choose-combo
                        (reify ActionListener
                          (actionPerformed
                            [_ evt]
                            (def selectedtable (.getSelectedItem choose-combo))
                            (def columndata (get-table-columns db (.getSelectedItem choose-combo)))
                            (def tabledata (get-table-data db (.getSelectedItem choose-combo)))
                            (def model (proxy [DefaultTableModel]  [tabledata columndata]))
                            (.setModel table model))))

                      (doto choose-frame
                        (.add choose-label)
                        (.add choose-combo)))

          table-panel (JScrollPane. table)

          footer-panel (let [button-frame (JPanel.)
                             table-label (JLabel. "table options:")
                             export-button (JButton. "export")
                             cmd-button (JButton. "cmd")
                             entry-label (JLabel. "entry options:")
                             delete-button (JButton. "delete")
                             insert-button (JButton. "new")]

                         (.addActionListener
                           insert-button
                           (reify ActionListener
                             (actionPerformed
                               [_ evt]
                               ; EVENT NEW
                               (new-frame db)
                               )))
                         (.addActionListener
                           delete-button
                           (reify ActionListener
                             (actionPerformed
                               [_ evt]
                               ; EVENT DELETE
                               )))
                         (.addActionListener
                           export-button
                           (reify ActionListener
                             (actionPerformed
                               [_ evt]
                               ; EVENT EXPORT
                               )))
                          (.addActionListener
                           cmd-button
                           (reify ActionListener
                             (actionPerformed
                               [_ evt]
                                ; EVENT CMD
                                (cmd-frame db)
                               )))

                           (doto button-frame
                             (.add table-label)
                             (.add export-button)
                             (.add cmd-button)
                             (.add entry-label)
                             (.add delete-button)
                             (.add insert-button)))

          ;ToDo: Fix tableListener
          tListener   (proxy [TableModelListener] []
                        (tableChanged [event]
                          (println "Table Update!")))]
    (do
      (.setComponentOrientation pane ComponentOrientation/RIGHT_TO_LEFT)
      (.setModel table model)
      (.addTableModelListener model tListener)
        (doto pane
          (.add top-panel BorderLayout/PAGE_START)
          (.add table-panel BorderLayout/CENTER)
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
      (.pack)
      (.setVisible true))))

(databaseconnect)
