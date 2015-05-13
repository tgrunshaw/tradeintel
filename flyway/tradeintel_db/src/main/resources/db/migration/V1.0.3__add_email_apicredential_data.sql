/*
    @Version 31.10.13
    @Author: Tim Grunshaw
    This adds the email rows (verify email etc) and Kevs ApiCredential for use 
    with ListingRetreiver.

*/

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;



LOCK TABLES `email` WRITE;
/*!40000 ALTER TABLE `email` DISABLE KEYS */;
INSERT INTO `email` VALUES ('CHANGE_EMAIL_VERIFICATION','<body style=\"background-color:#F2F2F2\">\r\n         <div style=\"width:900px;margin:auto;background-color:white;padding:20px;\">\r\n         <p>Dear user,</p>\r\n         <p>It appears that you requested that you change your TradeIntel login email to this address. You must verify that this email address is correct by clicking on the following link:</p>\r\n         <a href=\"REPLACE_WITH_CALLBACK_URL\" style=\"border-radius:3px;background:#85b2cb;border:1px solid #85b2cb;color:#fff;display:inline-block;font-weight:700;line-height:20px;margin:20 auto 20px;padding:6px 15px;text-decoration:none\">Verify</a>\r\n         <p></p>\r\n         If above link doesn\'t work then copy/paste the following URL into your browser:\r\n         <p></p>\r\n         REPLACE_WITH_CALLBACK_URL\r\n         <p></p>\r\n         If you have any questions or if there\'s any way we can be of assistance, please do not hesitate to contact us.\r\n         <p></p>\r\n         Thank you,\r\n         <p></p>\r\n         TradeIntel Support<br/>\r\n         <a href=\"mailto:support@tradeintel.co.nz\" target=\"_blank\">support@tradeintel.co.nz</a>\r\n         </div>\r\n         </body>','TradeIntel - Please verify your new email.'),('RESET_PASSWORD_EMAIL','<body style=\"background-color:#F2F2F2\">\r\n         <div style=\"width:900px;margin:auto;background-color:white;padding:20px;\">\r\n         <p>Dear user,</p>\r\n         <p>You requested that your password for TradeIntel.co.nz be reset. Please click the following link to reset your password:</p>\r\n         <a href=\"REPLACE_WITH_CALLBACK_URL\" style=\"border-radius:3px;background:#85b2cb;border:1px solid #85b2cb;color:#fff;display:inline-block;font-weight:700;line-height:20px;margin:20 auto 20px;padding:6px 15px;text-decoration:none\">Reset Password</a>\r\n         <p></p>\r\n         If above link doesn\'t work then copy/paste the following URL into your browser:\r\n         <p></p>\r\n         REPLACE_WITH_CALLBACK_URL\r\n         <p></p>\r\nIf this was not the case, then you can simply ignore this email and your password will not be changed.\r\n         If you have any questions or if there\'s any way we can be of assistance, please do not hesitate to contact us.\r\n         <p></p>\r\n         Thank you,\r\n         <p></p>\r\n         TradeIntel Support<br/>\r\n         <a href=\"mailto:support@tradeintel.co.nz\" target=\"_blank\">support@tradeintel.co.nz</a>\r\n         </div>\r\n         </body>','TradeIntel - Password reset notification'),('VERIFICATION_EMAIL','<body style=\"background-color:#F2F2F2\">\r\n        <div style=\"width:900px;margin:auto;background-color:white;padding:20px;\">\r\n        <p>Dear user,</p>\r\n        <p>Your new TradeIntel account is registerd to this email (email). Before you can login and use your new account, you must verify that this email address is correct by clicking on the following link:</p>\r\n        <a href=\"REPLACE_WITH_CALLBACK_URL\" style=\"border-radius:3px;background:#85b2cb;border:1px solid #85b2cb;color:#fff;display:inline-block;font-weight:700;line-height:20px;margin:20 auto 20px;padding:6px 15px;text-decoration:none\">Verify</a>\r\n        <p></p>\r\n        If above link doesn\'t work then copy/paste the following URL into your browser:\r\n        <p></p>\r\n        REPLACE_WITH_CALLBACK_URL\r\n        <p></p>\r\n        If you have any questions or if there\'s any way we can be of assistance, please do not hesitate to contact us.\r\n        <p></p>\r\n        Thank you,\r\n        <p></p>\r\n        TradeIntel Support<br/>\r\n        <a href=\"mailto:support@tradeintel.co.nz\" target=\"_blank\">support@tradeintel.co.nz</a>\r\n        </div>\r\n        </body>','Welcome to TradeIntel - Please verify your email.');
/*!40000 ALTER TABLE `email` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `apicredentials` WRITE;
/*!40000 ALTER TABLE `apicredentials` DISABLE KEYS */;
INSERT INTO `apicredentials` VALUES ('04E29651BDB4802CF06FAA52A18A325129','TradeMeApiCredentials','868988C0F966A9102248D3EEAEE2F70770','B85618B92C547BDB75E28127C5576215C8','6F7FCB2CC556F67327A366B59A82FC902B','2013-07-01 15:35:51','2013-10-17 10:00:01','k.a.doran1@gmail.com','Kevin','Doran','kad78',19989,'LISTING_DETAIL_UPDATER'),('C9D4CF25C753FF4312C0DE249CA0A6D3B8','TradeMeApiCredentials','9F7EC8AFE463D0D4231A6ED15B8BC48774','B85618B92C547BDB75E28127C5576215C8','6F7FCB2CC556F67327A366B59A82FC902B','2013-10-16 14:07:50','1970-01-01 12:00:00','tgrunshaw@gmail.com','Tim','Grunshaw','bobcob',0,'NO_RESTRICTION');
/*!40000 ALTER TABLE `apicredentials` ENABLE KEYS */;
UNLOCK TABLES;


/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;