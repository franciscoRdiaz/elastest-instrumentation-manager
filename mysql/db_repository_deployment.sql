/*
SQLyog Community Edition- MySQL GUI v6.05
Host - 5.7.20-log : Database - eim
*********************************************************************
Server version : 5.7.20-log
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

create database if not exists `eim`;

USE `eim`;

/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

/*Table structure for table `agent` */

DROP TABLE IF EXISTS `agent`;

CREATE TABLE `agent` (
  `agentId` varchar(255) NOT NULL,
  `host` varchar(255) NOT NULL,
  `monitored` tinyint(1) NOT NULL,
  `logstash_ip` varchar(255) NOT NULL,
  `logstash_port` varchar(255) NOT NULL,
  PRIMARY KEY (`agentId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

/*Table structure for table `agentconfiguration` */

DROP TABLE IF EXISTS `agentconfiguration`;

CREATE TABLE `agentconfiguration` (
  `agentId` varchar(255) NOT NULL,
  `exec` varchar(255) NOT NULL,
  `component` varchar(255) NOT NULL,
  `packetbeat_stream` varchar(255) NOT NULL,
  `topbeat_stream` varchar(255) NOT NULL,
  `filebeat_stream` varchar(255) NOT NULL,
  `filebeat_paths` text NOT NULL,
  PRIMARY KEY (`agentId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
