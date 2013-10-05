# --------------------------------------------------------
# Host:                         127.0.0.1
# Server version:               5.5.11
# Server OS:                    Win64
# HeidiSQL version:             6.0.0.3603
# Date/time:                    2013-10-05 23:05:50
# --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

# Dumping structure for table smslib.smslib_calls
CREATE TABLE IF NOT EXISTS `smslib_calls` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `caller_id` varchar(16) NOT NULL,
  `gateway_id` varchar(32) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

# Data exporting was unselected.


# Dumping structure for table smslib.smslib_gateways
CREATE TABLE IF NOT EXISTS `smslib_gateways` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `class` varchar(64) NOT NULL,
  `gateway_id` varchar(32) NOT NULL,
  `p0` varchar(32) NOT NULL,
  `p1` varchar(32) NOT NULL,
  `p2` varchar(32) NOT NULL,
  `p3` varchar(32) NOT NULL,
  `p4` varchar(32) NOT NULL,
  `p5` varchar(32) NOT NULL,
  `sender_id` varchar(16) NOT NULL,
  `priority` int(11) NOT NULL DEFAULT '0',
  `max_message_parts` int(11) NOT NULL DEFAULT '2',
  `delivery_reports` int(11) NOT NULL DEFAULT '0',
  `profile` varchar(32) NOT NULL,
  `is_enabled` int(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `gateway_id` (`gateway_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

# Data exporting was unselected.


# Dumping structure for table smslib.smslib_groups
CREATE TABLE IF NOT EXISTS `smslib_groups` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `group_name` varchar(32) NOT NULL,
  `group_description` varchar(100) NOT NULL,
  `profile` varchar(32) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `group_name` (`group_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

# Data exporting was unselected.


# Dumping structure for table smslib.smslib_group_recipients
CREATE TABLE IF NOT EXISTS `smslib_group_recipients` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `group_id` int(10) NOT NULL,
  `recipient` varchar(16) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

# Data exporting was unselected.


# Dumping structure for table smslib.smslib_in
CREATE TABLE IF NOT EXISTS `smslib_in` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `originator` varchar(16) NOT NULL,
  `encoding` varchar(1) NOT NULL,
  `text` varchar(4096) NOT NULL,
  `message_date` datetime NOT NULL,
  `receive_date` datetime NOT NULL,
  `gateway_id` varchar(32) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

# Data exporting was unselected.


# Dumping structure for table smslib.smslib_number_routes
CREATE TABLE IF NOT EXISTS `smslib_number_routes` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `regex` varchar(128) NOT NULL,
  `gateway_id` varchar(32) NOT NULL,
  `is_enabled` int(1) NOT NULL DEFAULT '0',
  `profile` varchar(32) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

# Data exporting was unselected.


# Dumping structure for table smslib.smslib_out
CREATE TABLE IF NOT EXISTS `smslib_out` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `parent_id` int(10) NOT NULL DEFAULT '0',
  `message_id` varchar(128) NOT NULL,
  `create_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `sender_id` varchar(16) NOT NULL DEFAULT '',
  `recipient` varchar(16) NOT NULL,
  `text` varchar(1024) NOT NULL,
  `encoding` varchar(1) NOT NULL DEFAULT '7',
  `priority` int(11) NOT NULL DEFAULT '0',
  `request_delivery_report` int(11) NOT NULL DEFAULT '0',
  `flash_sms` int(11) NOT NULL DEFAULT '0',
  `sent_status` varchar(1) NOT NULL DEFAULT 'U',
  `sent_date` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `gateway_id` varchar(32) NOT NULL DEFAULT '',
  `operator_message_id` varchar(128) NOT NULL DEFAULT '',
  `delivery_status` varchar(1) NOT NULL DEFAULT '',
  `delivery_date` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`id`),
  UNIQUE KEY `message_id` (`message_id`),
  KEY `sent_status` (`sent_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

# Data exporting was unselected.
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
