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

(ns ClojureMySQLEditor.view)

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
  )

; Warnungsfenster bei Fehler
(defn error-frame
  [err-reason]
  (println (str "Error: " err-reason))
  (let [
        err-frame (JFrame. "Error Message")
        
        err-top-panel (JPanel.)
        err-top-label (JLabel. "Reason:")
        
        err-center-panel (JPanel.)
        err-center-label (JLabel. err-reason)
        
        err-footer-panel (JPanel.)
        err-footer-button (JButton. "Close")
       ]
    ; Farbe setzen
    (.setForeground err-top-label (. Color red))
    
    ;ActionListener für Close Button
    (.addActionListener
      err-footer-button
      (reify ActionListener
        (actionPerformed
          [_ evt]
          ; EVENT CMD
          (.setVisible err-frame false))))
    
    (doto err-top-panel
      (.add err-top-label))
    
    (doto err-center-panel
      (.add err-center-label))
    
    (doto err-footer-panel
      (.add err-footer-button))
    
    (doto err-frame
      (.add err-top-panel BorderLayout/PAGE_START)
      (.add err-center-panel BorderLayout/CENTER)
      (.add err-footer-panel BorderLayout/PAGE_END)
      (.setSize 350 175)
      (.setVisible true))))