SET FOREIGN_KEY_CHECKS=0;
DROP DATABASE IF EXISTS maxigestion_db;
CREATE DATABASE maxigestion_db;
USE maxigestion_db;

-- MySQL dump 10.13  Distrib 8.0.41, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: maxigestion_db
-- ------------------------------------------------------
-- Server version	8.0.41

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `ciudades`
--

DROP TABLE IF EXISTS `ciudades`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ciudades` (
  `id_ciudad` int NOT NULL,
  `nombre_ciudad` varchar(255) DEFAULT NULL,
  `id_departamento` int DEFAULT NULL,
  PRIMARY KEY (`id_ciudad`),
  KEY `fk_ciudades_departamento` (`id_departamento`),
  CONSTRAINT `fk_ciudades_departamento` FOREIGN KEY (`id_departamento`) REFERENCES `departamento` (`id_departamento`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ciudades`
--

LOCK TABLES `ciudades` WRITE;
/*!40000 ALTER TABLE `ciudades` DISABLE KEYS */;
INSERT INTO `ciudades` VALUES (1,'Medellín',1),(2,'Itaguí',1);
/*!40000 ALTER TABLE `ciudades` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `departamento`
--

DROP TABLE IF EXISTS `departamento`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `departamento` (
  `id_departamento` int NOT NULL,
  `nombre_departamento` varchar(100) NOT NULL,
  PRIMARY KEY (`id_departamento`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `departamento`
--

LOCK TABLES `departamento` WRITE;
/*!40000 ALTER TABLE `departamento` DISABLE KEYS */;
INSERT INTO `departamento` VALUES (1,'Antioquia'),(2,'Cundinamarca');
/*!40000 ALTER TABLE `departamento` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `detalle_ordenes`
--

DROP TABLE IF EXISTS `detalle_ordenes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `detalle_ordenes` (
  `cantidad_producto` int DEFAULT NULL,
  `valor_producto` decimal(38,2) DEFAULT NULL,
  `id_factura` bigint NOT NULL,
  `id_terminado` int NOT NULL,
  `descripcion_producto` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id_factura`,`id_terminado`),
  KEY `fk_detalle_ordenes_terminado` (`id_terminado`),
  CONSTRAINT `detalle_ordenes_ibfk_1` FOREIGN KEY (`id_factura`) REFERENCES `ordenes` (`id_factura`),
  CONSTRAINT `fk_detalle_ordenes_terminado` FOREIGN KEY (`id_terminado`) REFERENCES `terminados` (`id_terminado`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `detalle_ordenes`
--

LOCK TABLES `detalle_ordenes` WRITE;
/*!40000 ALTER TABLE `detalle_ordenes` DISABLE KEYS */;
INSERT INTO `detalle_ordenes` VALUES (3,75000.00,29,1,'Sesgador doble y sencillo - Medida: 4'),(3,75000.00,30,1,'Sesgador doble y sencillo - Medida: 4'),(1,85000.00,30,2,'Sesgador doble y sencillo - Medida: 4.5'),(2,60000.00,30,7,'Guía de sesgar - Medida: 1.6'),(2,75000.00,31,1,'Sesgador doble y sencillo - Medida: 4'),(4,43000.00,32,1,'Sesgador doble y sencillo - Medida: 4'),(2,75000.00,33,1,'Sesgador doble y sencillo - Medida: 4'),(1,75000.00,34,1,'Sesgador doble y sencillo - Medida: 4'),(2,16000.00,35,7,'Guía de sesgar - Medida: 1.6'),(2,60000.00,36,7,'Guía de sesgar - Medida: 1.6'),(1,17000.00,37,2,'Sesgador doble y sencillo - Medida: 4.5'),(11,30000.00,37,7,'Guía de sesgar - Medida: 1.6'),(6,75000.00,38,1,'Sesgador doble y sencillo - Medida: 4'),(1,16000.00,39,7,'Guía de sesgar - Medida: 1.6'),(1,75000.00,40,1,'Sesgador doble y sencillo - Medida: 4'),(4,60000.00,41,7,'Guía de sesgar - Medida: 1.6'),(3,75000.00,42,1,'Sesgador doble y sencillo - Medida: 4'),(3,60000.00,43,7,'Guía de sesgar - Medida: 1.6'),(3,30000.00,44,7,'Guía de sesgar - Medida: 1.6'),(5,30000.00,45,7,'Guía de sesgar - Medida: 1.6'),(1,60000.00,46,7,'Guía de sesgar - Medida: 1.6'),(4,43000.00,47,1,'Sesgador doble y sencillo - Medida: 4'),(4,45000.00,48,2,'Sesgador doble y sencillo - Medida: 4.5');
/*!40000 ALTER TABLE `detalle_ordenes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `empresas`
--

DROP TABLE IF EXISTS `empresas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `empresas` (
  `nit_empresa` varchar(20) NOT NULL,
  `nombre_empresa` varchar(255) DEFAULT NULL,
  `fecha_creacion_empresa` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`nit_empresa`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `empresas`
--

LOCK TABLES `empresas` WRITE;
/*!40000 ALTER TABLE `empresas` DISABLE KEYS */;
INSERT INTO `empresas` VALUES ('900123456','MaxiGuia','2020-01-15 00:00:00.000000');
/*!40000 ALTER TABLE `empresas` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `formularios`
--

DROP TABLE IF EXISTS `formularios`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `formularios` (
  `id_formulario` int NOT NULL,
  `nombre_formulario` varchar(50) NOT NULL,
  `url` varchar(255) NOT NULL,
  `padre` int DEFAULT NULL,
  PRIMARY KEY (`id_formulario`),
  KEY `padre` (`padre`),
  CONSTRAINT `formularios_ibfk_1` FOREIGN KEY (`padre`) REFERENCES `formularios` (`id_formulario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `formularios`
--

LOCK TABLES `formularios` WRITE;
/*!40000 ALTER TABLE `formularios` DISABLE KEYS */;
INSERT INTO `formularios` VALUES (1,'Dashboard','/',NULL),(2,'Usuarios','/usuarios',NULL),(3,'Productos','/productos',NULL),(4,'Órdenes','/ordenes',NULL),(5,'Reportes','/reportes',NULL),(6,'Crear_Usuarios','/usuarios/crear',NULL),(7,'Crear_Ordenes','/ordenes/nueva',NULL),(8,'Buscar_Usuario_Ordenes','/ordenes/buscar-usuarios',NULL),(9,'Crear_Ordenes','/ordenes/guardar',NULL),(10,'Ver_Ordenes','ordenes/verMas',NULL),(11,'Crear_Productos','/productos/nuevo',NULL),(12,'Ver_Catalogo','/productos/catalogo',NULL),(13,'Editar_Ordenes','/ordenes/editar',NULL),(14,'Ver_Terminados','/terminados/por-producto/',NULL),(15,'Ver_Catalogo','/productos/juridico',NULL);
/*!40000 ALTER TABLE `formularios` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `formularios_x_perfiles`
--

DROP TABLE IF EXISTS `formularios_x_perfiles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `formularios_x_perfiles` (
  `perfiles_id_perfil` int NOT NULL,
  `formularios_id_formulario` int NOT NULL,
  `crear` char(1) NOT NULL,
  `editar` char(1) NOT NULL,
  `visualizar` char(1) NOT NULL,
  `eliminar` char(1) NOT NULL,
  PRIMARY KEY (`perfiles_id_perfil`,`formularios_id_formulario`),
  KEY `formularios_id_formulario` (`formularios_id_formulario`),
  CONSTRAINT `formularios_x_perfiles_ibfk_1` FOREIGN KEY (`perfiles_id_perfil`) REFERENCES `perfiles` (`id_perfil`),
  CONSTRAINT `formularios_x_perfiles_ibfk_2` FOREIGN KEY (`formularios_id_formulario`) REFERENCES `formularios` (`id_formulario`),
  CONSTRAINT `formularios_x_perfiles_ibfk_3` FOREIGN KEY (`perfiles_id_perfil`) REFERENCES `perfiles` (`id_perfil`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `formularios_x_perfiles`
--

LOCK TABLES `formularios_x_perfiles` WRITE;
/*!40000 ALTER TABLE `formularios_x_perfiles` DISABLE KEYS */;
INSERT INTO `formularios_x_perfiles` VALUES (1,1,'S','S','S','S'),(1,2,'S','S','S','S'),(1,3,'S','S','S','S'),(1,4,'S','S','S','S'),(1,5,'S','S','S','S'),(1,6,'S','S','S','S'),(1,7,'S','S','S','S'),(1,8,'S','S','S','S'),(1,9,'S','S','S','S'),(1,10,'S','S','S','S'),(1,11,'S','S','S','S'),(1,12,'S','S','S','S'),(1,13,'S','S','S','S'),(2,4,'S','S','S','N'),(2,12,'N','N','S','N'),(2,14,'N','N','S','N'),(2,15,'S','S','S','S');
/*!40000 ALTER TABLE `formularios_x_perfiles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ordenes`
--

DROP TABLE IF EXISTS `ordenes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ordenes` (
  `fecha_entrega` datetime DEFAULT NULL,
  `total_factura` decimal(38,2) DEFAULT NULL,
  `documento_usuario` bigint DEFAULT NULL,
  `id_factura` bigint NOT NULL AUTO_INCREMENT,
  `descripcion_venta` varchar(255) DEFAULT NULL,
  `firma_digital` varchar(255) DEFAULT NULL,
  `id_empresa` varchar(255) DEFAULT NULL,
  `estado` varchar(20) NOT NULL DEFAULT 'PENDIENTE',
  `fecha_orden` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_factura`),
  KEY `FK2hr37hoth4u3h7mcxvv52prxw` (`id_empresa`),
  KEY `FK86cmsmi3ki6tondwhx3fxbdhx` (`documento_usuario`),
  CONSTRAINT `FK2hr37hoth4u3h7mcxvv52prxw` FOREIGN KEY (`id_empresa`) REFERENCES `empresas` (`nit_empresa`),
  CONSTRAINT `FK86cmsmi3ki6tondwhx3fxbdhx` FOREIGN KEY (`documento_usuario`) REFERENCES `usuarios` (`documento`)
) ENGINE=InnoDB AUTO_INCREMENT=49 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ordenes`
--

LOCK TABLES `ordenes` WRITE;
/*!40000 ALTER TABLE `ordenes` DISABLE KEYS */;
INSERT INTO `ordenes` VALUES ('2025-10-24 17:46:00',225000.00,10119372,29,'Mil test',NULL,'900123456','FACTURADA','2025-10-15 21:14:13'),('2025-10-01 17:46:00',430000.00,10119372,30,'Mil test',NULL,'900123456','FACTURADA','2025-10-18 12:47:18'),('2025-10-18 21:09:00',150000.00,10119372,31,'Mil test',NULL,'900123456','FACTURADA','2025-10-18 14:09:15'),('2025-10-15 19:19:00',172000.00,2020202020,32,'Mil test',NULL,'900123456','PENDIENTE','2025-10-18 14:19:40'),('2025-10-31 19:20:00',150000.00,1032403274389,33,'Mil test',NULL,'900123456','PENDIENTE','2025-10-18 14:20:26'),('2025-10-29 19:21:00',75000.00,10119372,34,'TEST',NULL,'900123456','PENDIENTE','2025-10-18 14:21:10'),('2025-10-29 19:21:00',32000.00,2020202020,35,'TEST',NULL,'900123456','PENDIENTE','2025-10-18 14:21:39'),('2025-10-03 19:21:00',120000.00,1032403274389,36,'Mil test',NULL,'900123456','PENDIENTE','2025-10-18 14:22:07'),('2025-10-09 19:22:00',347000.00,9876542,37,'TEST',NULL,'900123456','EN_PROCESO','2025-10-18 14:22:32'),('2025-10-03 19:22:00',450000.00,1032403274389,38,'Mil test',NULL,'900123456','PENDIENTE','2025-10-18 14:22:56'),('2025-10-18 23:23:00',16000.00,2020202020,39,'Mil test',NULL,'900123456','PENDIENTE','2025-10-18 14:23:19'),('2025-11-01 01:38:00',75000.00,974829,40,'Mil test',NULL,'900123456','FINALIZADA','2025-10-22 20:38:26'),('2025-10-31 01:47:00',240000.00,974829,41,'TEST',NULL,'900123456','PENDIENTE','2025-10-22 20:47:20'),('2025-10-30 23:24:00',225000.00,10119372,42,'Venta wilmer',NULL,'900123456','FINALIZADA','2025-10-24 18:24:49'),('2025-10-31 23:54:00',180000.00,10119372,43,'TEST',NULL,'900123456','PENDIENTE','2025-10-25 18:54:17'),('2025-11-01 00:07:00',90000.00,9876542,44,'TEST JURIDICO 32',NULL,'900123456','CANCELADA','2025-10-25 19:08:27'),('2025-11-07 02:11:00',150000.00,9876542,45,'Mil test',NULL,'900123456','PENDIENTE','2025-10-25 21:11:59'),('2025-10-26 03:16:00',60000.00,10119372,46,'FECHA TEST',NULL,'900123456','PENDIENTE','2025-10-25 21:16:50'),('2025-11-07 22:57:00',172000.00,9876542,47,'VENTA TEST',NULL,'900123456','FACTURADA','2025-10-31 17:57:22'),('2025-11-08 18:06:00',180000.00,9876542,48,'Venta suprema',NULL,'900123456','FINALIZADA','2025-10-31 18:07:17');
/*!40000 ALTER TABLE `ordenes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `perfiles`
--

DROP TABLE IF EXISTS `perfiles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `perfiles` (
  `id_perfil` int NOT NULL AUTO_INCREMENT,
  `nombre_perfil` varchar(50) NOT NULL,
  `roles_id_rol` int NOT NULL,
  PRIMARY KEY (`id_perfil`),
  KEY `perfiles_ibfk_1` (`roles_id_rol`),
  CONSTRAINT `perfiles_ibfk_1` FOREIGN KEY (`roles_id_rol`) REFERENCES `roles` (`id_rol`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `perfiles`
--

LOCK TABLES `perfiles` WRITE;
/*!40000 ALTER TABLE `perfiles` DISABLE KEYS */;
INSERT INTO `perfiles` VALUES (1,'DEVELOPER',1),(2,'ALMACEN',2),(4,'NUEVO2',1),(5,'ADMIN_GENERAL',1),(8,'TEST',2);
/*!40000 ALTER TABLE `perfiles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `productos`
--

DROP TABLE IF EXISTS `productos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `productos` (
  `id_producto` int NOT NULL,
  `nombre_guia` varchar(255) DEFAULT NULL,
  `imagen_producto` varchar(255) DEFAULT NULL,
  `cantidad_disponible` int DEFAULT NULL,
  PRIMARY KEY (`id_producto`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `productos`
--

LOCK TABLES `productos` WRITE;
/*!40000 ALTER TABLE `productos` DISABLE KEYS */;
INSERT INTO `productos` VALUES (1,'Sesgador doble y sencillo','GUIA1_bcexnt.jpg#1759885078',0),(2,'Guía de sesgar','GuiaRTest_hxl2tf.png#1759885201',10),(3243,'Guía costura básica','assets/images/GuiaR.png',10),(32432,'Guía costura básica','assets/images/GuiaR.png',10),(65432,'Producto de prueba','MAXYGUIAS/Imagenes-Productos/nyx07jrztvs49i1nssx5#1761348225',NULL),(789374,'TEST PROD','MAXYGUIAS/Imagenes-Productos/a5mhqnnbzcajf2oogjeb#1761180337',NULL);
/*!40000 ALTER TABLE `productos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `roles`
--

DROP TABLE IF EXISTS `roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `roles` (
  `id_rol` int NOT NULL AUTO_INCREMENT,
  `nombre_rol` varchar(50) NOT NULL,
  PRIMARY KEY (`id_rol`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `roles`
--

LOCK TABLES `roles` WRITE;
/*!40000 ALTER TABLE `roles` DISABLE KEYS */;
INSERT INTO `roles` VALUES (1,'ADMINISTRADOR'),(2,'JURIDICO');
/*!40000 ALTER TABLE `roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `terminados`
--

DROP TABLE IF EXISTS `terminados`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `terminados` (
  `id_terminado` int NOT NULL AUTO_INCREMENT,
  `id_producto` int NOT NULL,
  `medida_terminado_producto` decimal(4,2) NOT NULL,
  `precio_publico` int NOT NULL,
  `precio_x_mayor` int NOT NULL,
  `ganancia_x_mayor` decimal(5,2) DEFAULT NULL,
  `precio_x_encargo` int NOT NULL,
  `ganancia_x_encargo` decimal(5,2) DEFAULT NULL,
  PRIMARY KEY (`id_terminado`),
  KEY `id_producto` (`id_producto`),
  CONSTRAINT `terminados_ibfk_1` FOREIGN KEY (`id_producto`) REFERENCES `productos` (`id_producto`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `terminados`
--

LOCK TABLES `terminados` WRITE;
/*!40000 ALTER TABLE `terminados` DISABLE KEYS */;
INSERT INTO `terminados` VALUES (1,1,4.00,75000,43000,NULL,15000,NULL),(2,1,4.50,85000,45000,NULL,17000,NULL),(7,2,1.60,60000,30000,NULL,16000,NULL),(11,789374,2.00,129129,10000,15.00,2912,13.20),(12,65432,1.00,10000,25000,10.00,12000,10.00),(13,3243,1.20,1000,21223,10.00,31332,11.00),(14,32432,1.20,1000,21223,10.00,31332,11.00);
/*!40000 ALTER TABLE `terminados` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tipo_usuario`
--

DROP TABLE IF EXISTS `tipo_usuario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tipo_usuario` (
  `id_tipo_usuario` int NOT NULL,
  `nombre_tipo_usuario` varchar(30) NOT NULL,
  PRIMARY KEY (`id_tipo_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tipo_usuario`
--

LOCK TABLES `tipo_usuario` WRITE;
/*!40000 ALTER TABLE `tipo_usuario` DISABLE KEYS */;
INSERT INTO `tipo_usuario` VALUES (1,'ADMIN'),(2,'NATURAL'),(3,'JURIDICO');
/*!40000 ALTER TABLE `tipo_usuario` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuarios`
--

DROP TABLE IF EXISTS `usuarios`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuarios` (
  `documento` bigint NOT NULL,
  `telefono` bigint DEFAULT NULL,
  `contrasena` varchar(255) NOT NULL,
  `usuario` varchar(30) DEFAULT NULL,
  `primer_apellido` varchar(50) DEFAULT NULL,
  `segundo_apellido` varchar(50) DEFAULT NULL,
  `nombre` varchar(100) NOT NULL,
  `direccion` varchar(255) DEFAULT NULL,
  `perfiles_id_perfil` int NOT NULL,
  `tipo_usuario` int NOT NULL,
  `id_ciudad` int NOT NULL,
  `fecha_registro` datetime DEFAULT CURRENT_TIMESTAMP,
  `CORREO` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`documento`),
  UNIQUE KEY `idx_usuarios_correo` (`CORREO`),
  KEY `fk_tipo_usuario` (`tipo_usuario`),
  KEY `fk_perfil_usuario` (`perfiles_id_perfil`),
  KEY `fk_usuarios_ciudades` (`id_ciudad`),
  CONSTRAINT `fk_perfil_usuario` FOREIGN KEY (`perfiles_id_perfil`) REFERENCES `perfiles` (`id_perfil`),
  CONSTRAINT `fk_tipo_usuario` FOREIGN KEY (`tipo_usuario`) REFERENCES `tipo_usuario` (`id_tipo_usuario`),
  CONSTRAINT `fk_usuarios_ciudades` FOREIGN KEY (`id_ciudad`) REFERENCES `ciudades` (`id_ciudad`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuarios`
--

LOCK TABLES `usuarios` WRITE;
/*!40000 ALTER TABLE `usuarios` DISABLE KEYS */;
INSERT INTO `usuarios` VALUES (103,3205727115,'$2a$10$lA8ajA5ygpp6kf3NEKBkMuy98aTLvaDrpjLcfdiagusizDF4posAe','MelSr6543','TEST','Suarez','TEST','Cra 48 #84-31',5,1,2,NULL,'test@gmail.com'),(6564,3205765537,'','','Builes','Castro','Julieth','Cll 9 #52',1,2,1,NULL,NULL),(78978,3205727115,'$2a$10$/J9XyPhO1gYi.ex7W21OUOXAB/xayRU6LBvlCPvyOPiMIp117.nFS','JURIDICOREGI','','','DENIS','Cra 48 #84-31',2,3,2,'2025-10-23 18:16:15','decariza@gmail.com'),(435343,423234,'$2a$10$YhTU.l5V.iALlMSR5g/HU.WeI1Kt47N1nQK0BnKqStZ8gMuEeXuZK','JUL923','MONCADA','SUAREZ','JULIETH','Cra 48 #84-31',5,1,1,'2025-10-23 21:25:29','juliethsuarez@gmail.com'),(974829,3205727115,'','','NATURAL','NATURAL','NATURAL','Cra 48 #84-31',8,2,2,'2025-10-22 20:32:11',NULL),(1011392,3200396564,'1234','MelanyS','Suarez','Rivera','Melany','Cll 36',1,1,1,NULL,NULL),(1389182,3205727115,'$2a$10$qyMDxl0VaZuTyAZ.zeVjC.qMnslLdbA0vTu8Rute4Z.g67N63s9fK','MelSr','Sur','Ri','Mel','Cll 35 #52 - 95',1,1,1,'2025-03-16 00:00:00','melanyjsuarez@gmail.com'),(9876542,8219,'$2a$10$aPxk/0UQb/vIoSBpq..6AeSAheVvCPFguBycK3ZTJaNuojS0oRJra','JURIDICO 32','','','JURIII TEST','CL 9832',2,3,2,NULL,NULL),(10119372,3205765537,'','','Gomez','Valencia','Wilmer','Cll 9 #52',1,2,1,NULL,NULL),(10309876,3205727115,'$2a$10$S2gG9ygFbSlJ5KffoX511uITf4.eqnr13yTNoxqXUmjQFV5V2URWu','MelSr5678','TEST','Suarez','TEST','Cra 48 #84-31',5,1,2,'2025-10-18 13:41:20','testmel@gmail.com'),(101984293,3205765537,'123456789','NicoB','Bernal','Bernal','Nicol','Cra 48 b sur',1,1,1,NULL,NULL),(2020202020,3119876543,'2432','Textil Fast','','','Textil Fast','Carrera 45',2,3,1,NULL,NULL),(8657474121,3205765537,'$2a$10$vsXxJMpHxP5BktvC/Kq83eMlWc32zW5e6qc6HgqQfdG4LEp7g0lAi','MelSr673','Builes','Castro','Julieth','Cll 9 #52',5,1,1,NULL,'melany_suarez23231@elpoli.edu.co'),(10119328999,821963452352,'$2a$10$suCX.ZJN1odJuLf81i6TjeFzO1RdG/WSdXLbDZ6/O0MIz0tq9saNu','nicolas.rodriguezm27','TEST','MEJIA','JURIII TEST MEL ANGULAR','CL 9832',1,1,1,'2025-10-27 20:30:00','nicolas.rodriguezm27@gmail.com'),(12345654321,3205724321,'$2a$10$o86Yqws7FX060CjL2Gy7dOo8i9/wQN7tBxvyG.bxXNAJgaT9i/xum','NicoRM','RODRIGUEZ','MEJIA','NICOLAS','Cra 48 #84-31',1,1,1,'2025-10-26 23:34:04',NULL),(101823792789,3205727115,'$2a$10$wxm.5hQOZtvWg8..tnginu6fS5hZeP1ZYboES8Xi3V/IQT6E6s0tK','WTEXTIL',NULL,NULL,'FIILM','Cll 35 #52 - 95',2,3,2,'2025-10-23 18:24:42',NULL),(1032403274389,3205727115,'','','TEST','Suarez','TEST','Cra 48 #84-31',4,2,2,NULL,NULL);
/*!40000 ALTER TABLE `usuarios` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-11-01 16:28:33

SET FOREIGN_KEY_CHECKS=1;
