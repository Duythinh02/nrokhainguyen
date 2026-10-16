/*M!999999\- enable the sandbox mode */ 
-- MariaDB dump 10.19  Distrib 10.6.19-MariaDB, for Win64 (AMD64)
--
-- Host: localhost    Database: nro_merge
-- ------------------------------------------------------
-- Server version	10.4.32-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `shop_ky_gui`
--

DROP TABLE IF EXISTS `shop_ky_gui`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shop_ky_gui` (
  `id` int(11) NOT NULL,
  `player_id` int(11) NOT NULL,
  `tab` int(11) NOT NULL,
  `item_id` int(11) NOT NULL,
  `gold` int(11) NOT NULL,
  `gem` int(11) NOT NULL,
  `quantity` int(11) NOT NULL,
  `itemOption` text NOT NULL,
  `isUpTop` int(11) NOT NULL,
  `isBuy` int(11) NOT NULL,
  `player_name` text NOT NULL DEFAULT '',
  `lastTime` bigint(11) NOT NULL DEFAULT 0,
  `coin` int(11) NOT NULL DEFAULT -1,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shop_ky_gui`
--

LOCK TABLES `shop_ky_gui` WRITE;
/*!40000 ALTER TABLE `shop_ky_gui` DISABLE KEYS */;
INSERT INTO `shop_ky_gui` VALUES (1,1154,3,19,10000000,-1,1,'[{\"id\":\"73\",\"param\":\"0\"}]',0,1,'',0,-1),(2,1179,3,19,50,-1,1,'[{\"id\":\"73\",\"param\":\"0\"}]',0,1,'',0,-1),(3,1184,3,20,1,-1,1,'[{\"id\":\"73\",\"param\":\"0\"}]',0,0,'',0,-1),(4,1391,3,19,66666,-1,3,'[{\"id\":\"73\",\"param\":\"0\"}]',0,0,'',0,-1),(5,1371,3,18,1,-1,1,'[{\"id\":\"73\",\"param\":\"0\"}]',0,1,'',0,-1),(6,1419,3,16,1,-1,50,'[{\"id\":\"73\",\"param\":\"0\"}]',0,1,'',0,-1),(8,1371,3,18,100000,-1,1,'[{\"id\":\"73\",\"param\":\"0\"}]',0,0,'',0,-1);
/*!40000 ALTER TABLE `shop_ky_gui` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-15 23:20:49
