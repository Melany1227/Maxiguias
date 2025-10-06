-- MySQL dump 10.13  Distrib 8.0.43, for Win64 (x86_64)
--
-- Host: localhost    Database: maxigestion_db
-- ------------------------------------------------------
-- Server version	8.0.43

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
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
INSERT INTO `ciudades` VALUES (1,'Medellín',1);
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
  `id_producto` int NOT NULL,
  `descripcion_producto` varchar(255) DEFAULT NULL,
  `id_detalle_factura` bigint NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id_detalle_factura`),
  KEY `id_factura` (`id_factura`),
  KEY `fk_detalle_factura_producto` (`id_producto`),
  CONSTRAINT `detalle_ordenes_ibfk_1` FOREIGN KEY (`id_factura`) REFERENCES `ordenes` (`id_factura`),
  CONSTRAINT `fk_detalle_factura_producto` FOREIGN KEY (`id_producto`) REFERENCES `productos` (`id_producto`)
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `detalle_ordenes`
--

LOCK TABLES `detalle_ordenes` WRITE;
/*!40000 ALTER TABLE `detalle_ordenes` DISABLE KEYS */;
INSERT INTO `detalle_ordenes` VALUES (5,43000.00,10,1,'Sesgador doble y sencillo - 4',25),(5,45000.00,10,1,'Sesgador doble y sencillo - 4.5',26),(7,43000.00,11,1,'Sesgador doble y sencillo - 4',27),(1,17000.00,11,1,'Sesgador doble y sencillo - 4.5',28),(4,43000.00,12,1,'Sesgador doble y sencillo - 4',29),(1,17000.00,12,1,'Sesgador doble y sencillo - 4.5',30),(9,45000.00,13,1,'Sesgador doble y sencillo - 4.5',31),(15,43000.00,13,1,'Sesgador doble y sencillo - 4',32),(4,43000.00,14,1,'Sesgador doble y sencillo - 4',33),(1,17000.00,14,1,'Sesgador doble y sencillo - 4.5',34);
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
/*!40000 ALTER TABLE `formularios_x_perfiles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ordenes`
--

DROP TABLE IF EXISTS `ordenes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ordenes` (
  `fecha_venta` date DEFAULT NULL,
  `lugar_venta` int DEFAULT NULL,
  `total_factura` decimal(38,2) DEFAULT NULL,
  `documento_usuario` bigint DEFAULT NULL,
  `id_factura` bigint NOT NULL AUTO_INCREMENT,
  `descripcion_venta` varchar(255) DEFAULT NULL,
  `firma_digital` varchar(255) DEFAULT NULL,
  `id_empresa` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id_factura`),
  KEY `FK1jppc9ai3o70qqa4eav9obpnq` (`lugar_venta`),
  KEY `FK2hr37hoth4u3h7mcxvv52prxw` (`id_empresa`),
  KEY `FK86cmsmi3ki6tondwhx3fxbdhx` (`documento_usuario`),
  CONSTRAINT `FK1jppc9ai3o70qqa4eav9obpnq` FOREIGN KEY (`lugar_venta`) REFERENCES `ciudades` (`id_ciudad`),
  CONSTRAINT `FK2hr37hoth4u3h7mcxvv52prxw` FOREIGN KEY (`id_empresa`) REFERENCES `empresas` (`nit_empresa`),
  CONSTRAINT `FK86cmsmi3ki6tondwhx3fxbdhx` FOREIGN KEY (`documento_usuario`) REFERENCES `usuarios` (`documento`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ordenes`
--

LOCK TABLES `ordenes` WRITE;
/*!40000 ALTER TABLE `ordenes` DISABLE KEYS */;
INSERT INTO `ordenes` VALUES ('2025-06-11',1,440000.00,2020202020,10,'TEST 2',NULL,'900123456'),('2025-06-12',1,318000.00,2020202020,11,'Venta suprema',NULL,'900123456'),('2025-06-06',1,189000.00,2020202020,12,'Venta suprema',NULL,'900123456'),('2025-06-13',1,1050000.00,2020202020,13,'TEST 2',NULL,'900123456'),('2025-06-03',1,189000.00,2020202020,14,'Venta suprema',NULL,'900123456');
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
  KEY `roles_id_rol` (`roles_id_rol`),
  CONSTRAINT `perfiles_ibfk_1` FOREIGN KEY (`roles_id_rol`) REFERENCES `roles` (`id_rol`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `perfiles`
--

LOCK TABLES `perfiles` WRITE;
/*!40000 ALTER TABLE `perfiles` DISABLE KEYS */;
INSERT INTO `perfiles` VALUES (1,'DEVELOPER',1),(2,'ALMACÉN',2);
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
INSERT INTO `productos` VALUES (1,'Sesgador doble y sencillo',NULL,0),(2,'Guía de sesgar',NULL,0);
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
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
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
  `id_terminado` int NOT NULL,
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `terminados`
--

LOCK TABLES `terminados` WRITE;
/*!40000 ALTER TABLE `terminados` DISABLE KEYS */;
INSERT INTO `terminados` VALUES (1,1,4.00,75000,43000,NULL,15000,NULL),(2,1,4.50,85000,45000,NULL,17000,NULL);
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
  `contrasena` varchar(255) DEFAULT NULL,
  `usuario` varchar(30) DEFAULT NULL,
  `primer_apellido` varchar(50) DEFAULT NULL,
  `segundo_apellido` varchar(50) DEFAULT NULL,
  `nombre` varchar(100) NOT NULL,
  `direccion` varchar(255) DEFAULT NULL,
  `perfiles_id_perfil` int NOT NULL,
  `tipo_usuario` int NOT NULL,
  `fecha_registro` date DEFAULT NULL,
  `id_ciudad` int NOT NULL,
  PRIMARY KEY (`documento`),
  UNIQUE KEY `UK3m5n1w5trapxlbo2s42ugwdmd` (`usuario`),
  KEY `fk_perfil_usuario` (`perfiles_id_perfil`),
  KEY `fk_tipo_usuario` (`tipo_usuario`),
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
INSERT INTO `usuarios` VALUES (1011392,3200396564,'1234','MelanyS','Suarez','Rivera','Melany','Cll 36',1,1,'2025-03-16',1),(10119372,3205765537,'','','Gomez','Valencia','Wilmer','Cll 9 #52',1,2,'2025-03-16',1),(101984293,3205765537,'123456789','NicoB','Bernal','Bernal','Nicol','Cra 48 b sur',1,1,NULL,1),(1000206922,3333333,'$2a$10$sz2U/zE84BdYp4aEv7xj.O4wk3EEN/zvru8BhWcg67KXX8gqJZNse','wilmergruiz','Gómez','Ruiz','Wilmer ','Carrera 78a #52sur-76',1,1,NULL,1),(2020202020,3119876543,'2432','Textil Fast','','','Textil Fast','Carrera 45',2,3,'2025-05-16',1);
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

-- Dump completed on 2025-10-02 11:59:51
