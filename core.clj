(ns DatabaseEditor.core)

(import '(javax.swing JFrame JLabel JTextField JButton)
        '(java.awt.event ActionListener)
        '(java.awt GridLayout)
        )
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
             [_ evt])))
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
