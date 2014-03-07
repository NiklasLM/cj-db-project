-- phpMyAdmin SQL Dump
-- version 3.4.5
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Erstellungszeit: 07. Mrz 2014 um 18:06
-- Server Version: 5.5.16
-- PHP-Version: 5.3.8

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Datenbank: `clojure`
--
CREATE DATABASE `clojure` DEFAULT CHARACTER SET latin1 COLLATE latin1_swedish_ci;
USE `clojure`;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `books`
--

CREATE TABLE IF NOT EXISTS `books` (
  `isbn` char(13) NOT NULL,
  `title` char(80) DEFAULT NULL,
  `fname` char(40) DEFAULT NULL,
  `author` char(40) DEFAULT NULL,
  `price` decimal(6,2) DEFAULT NULL,
  `year_published` int(11) DEFAULT NULL,
  PRIMARY KEY (`isbn`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Daten für Tabelle `books`
--

INSERT INTO `books` (`isbn`, `title`, `fname`, `author`, `price`, `year_published`) VALUES
('3-257-06209-5', 'Entwurf einer Liebe auf den ersten Blick', 'Erich', 'Hackl', 10.90, 1999),
('3-257-21755-2', 'Schwarzrock', 'Brian', 'Moore', 7.90, 1987),
('3-257-21931-8', 'Die Größe Viktorianische Sammlung', 'Brian', 'Moore', 8.90, 1990),
('3-422-72318-3', 'Längengrad', 'Dava', 'Zobel', 6.00, 1988),
('3-446-18298-5', 'Erklärt Pereira', 'Antonio', 'Tabucchi', 18.90, 1997),
('3-492-24118-2', 'Mit brennender Geduld', 'Antonio', 'Skärmeta', 8.00, 2004),
('3-499-22410-0', 'Geschichte machen', 'Stephen', 'Fry', 9.90, 1999),
('3-518-37459-1', 'Der Hauptmann und sein Frauenbataillon', 'Mario', 'Vargas Llosa', 8.80, 1984),
('3-518-38020-6', 'Tante Julia und der Kunstschreiber', 'Mario', 'Vargas Llosa', 7.80, 1988),
('3-596-13399-8', 'Glück Glanz Ruhm', 'Robert', 'Gernhardt', 5.90, 1997),
('3-8031-3112-X', 'Die nackten Masken', 'Luigi', 'Malerba', 23.00, 1995);

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `customers`
--

CREATE TABLE IF NOT EXISTS `customers` (
  `cid` int(11) NOT NULL,
  `cname` char(40) DEFAULT NULL,
  `address` char(200) DEFAULT NULL,
  `cardnum` char(16) DEFAULT NULL,
  PRIMARY KEY (`cid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `orderitems`
--

CREATE TABLE IF NOT EXISTS `orderitems` (
  `ordernum` int(11) NOT NULL DEFAULT '0',
  `isbn` char(13) NOT NULL DEFAULT '',
  `qty` int(11) DEFAULT NULL,
  PRIMARY KEY (`ordernum`,`isbn`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `orders`
--

CREATE TABLE IF NOT EXISTS `orders` (
  `ordernum` int(11) NOT NULL,
  `cid` int(11) DEFAULT NULL,
  `order_date` date DEFAULT NULL,
  PRIMARY KEY (`ordernum`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
