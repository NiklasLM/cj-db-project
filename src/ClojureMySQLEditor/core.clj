(ns ClojureMySQLEditor.core)

(use 'clojure.java.jdbc)

(import '(javax.swing JFrame JLabel JTextField JButton)
        '(java.awt.event ActionListener)
        '(java.awt GridLayout)
        '(java.sql.*))

(defn databaseconnect []
  (let [frame (JFrame. "Database Login")
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
                   db-name "a_database"]
 
               (def db {:classname "com.mysql.jdbc.Driver" 
                        :subprotocol (str (.getText protcol-text))
                        :subname (str "//"(.getText server-text)":"(.getText port-text)"/"(.getText database-text))
                        :user (str (.getText user-text))
                        :password (str (.getText password-text))}))
             
             (try (get-connection db)
               (.setText tmp-label "status: connected!")
               (println "Verbindung erfolgreich hergestellt!")
               (catch Exception e (println "Verbindung fehlgeschlagen!"))
               (finally (.setText tmp-label "status: disconnected!")))
             )))
    (doto frame
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
