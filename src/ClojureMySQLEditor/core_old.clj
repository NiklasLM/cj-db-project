(ns ClojureMySQLEditor.core)

(import '(javax.swing JFrame JLabel JTextField JButton)
        '(java.awt.event ActionListener)
        '(java.awt GridLayout))

(defn databaseconnect []
  (let [frame (JFrame. "Database Login")
       protcol-label (JLabel. "protocol:")
       protcol-text (JTextField. "mysql")
       database-name (JLabel. "database:")
       database-text (JTextField.)
       user-label (JLabel. "user:")
       user-text (JTextField.)
       password-label (JLabel. "password:")
       password-text (JTextField.)
       connect-button (JButton. "Connect")
       tmp-label (JLabel. "")]
    (.addActionListener
     connect-button
     (reify ActionListener
            (actionPerformed
             [_ evt]
             (use 'clojure.java.jdbc)
             
             (let [db-host "localhost"
                   db-port 3306
                   db-name "a_database"]
 
               (def db {:classname "com.mysql.jdbc.Driver" 
                        :subprotocol "mysql"
                        :subname (str "127.0.0.1:3306/clojure")
                        :user "root"
                        :password ""}))
             (with-connection db 
               (with-query-results rs ["select * from books"] 
                 (dorun (map #(println (:title %)) rs)))
             )))
    (doto frame
      (.setLayout (GridLayout. 4 2))
      (.add protcol-label)
    (.add protcol-text)
      (.add user-label)
    (.add user-text)
      (.add password-label)
    (.add password-text)
    (.add connect-button)
    (.add tmp-label)
      (.setSize 200 120)
      (.setVisible true))))
(databaseconnect)
