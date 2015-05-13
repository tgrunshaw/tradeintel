
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
DROP TABLE IF EXISTS `agency`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `agency` (
  `ID` int(11) NOT NULL,
  `ADDRESS` varchar(255) DEFAULT NULL,
  `AGENTS` longblob,
  `BILLINGTYPE` int(11) DEFAULT NULL,
  `CITY` varchar(255) DEFAULT NULL,
  `EMAIL` varchar(255) DEFAULT NULL,
  `FAXNUMBER` varchar(255) DEFAULT NULL,
  `ISJOBAGENCY` tinyint(1) DEFAULT '0',
  `ISLICENSEDPROPERTYAGENCY` tinyint(1) DEFAULT '0',
  `ISREALESTATEAGENCY` tinyint(1) DEFAULT '0',
  `LOGO` varchar(255) DEFAULT NULL,
  `NAME` varchar(255) DEFAULT NULL,
  `PHONENUMBER` varchar(255) DEFAULT NULL,
  `SUBURB` varchar(255) DEFAULT NULL,
  `TYPE` int(11) DEFAULT NULL,
  `WEBSITE` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `attribute`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `attribute` (
  `ID` int(11) NOT NULL,
  `DISPLAYNAME` varchar(255) DEFAULT NULL,
  `ISREQUIREDFORSELL` tinyint(1) DEFAULT '0',
  `NAME` varchar(255) DEFAULT NULL,
  `TYPE` varchar(255) DEFAULT NULL,
  `VALUE` varchar(255) DEFAULT NULL,
  `LOWER` varchar(255) DEFAULT NULL,
  `UPPER` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `attribute_attributeoption`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `attribute_attributeoption` (
  `Attribute_ID` int(11) NOT NULL,
  `options_ID` int(11) NOT NULL,
  PRIMARY KEY (`Attribute_ID`,`options_ID`),
  KEY `FK_ATTRIBUTE_ATTRIBUTEOPTION_options_ID` (`options_ID`),
  CONSTRAINT `FK_ATTRIBUTE_ATTRIBUTEOPTION_options_ID` FOREIGN KEY (`options_ID`) REFERENCES `attributeoption` (`ID`),
  CONSTRAINT `FK_ATTRIBUTE_ATTRIBUTEOPTION_Attribute_ID` FOREIGN KEY (`Attribute_ID`) REFERENCES `attribute` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `attribute_attributeunit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `attribute_attributeunit` (
  `Attribute_ID` int(11) NOT NULL,
  `units_ID` int(11) NOT NULL,
  PRIMARY KEY (`Attribute_ID`,`units_ID`),
  KEY `FK_ATTRIBUTE_ATTRIBUTEUNIT_units_ID` (`units_ID`),
  CONSTRAINT `FK_ATTRIBUTE_ATTRIBUTEUNIT_Attribute_ID` FOREIGN KEY (`Attribute_ID`) REFERENCES `attribute` (`ID`),
  CONSTRAINT `FK_ATTRIBUTE_ATTRIBUTEUNIT_units_ID` FOREIGN KEY (`units_ID`) REFERENCES `attributeunit` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `attributeoption`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `attributeoption` (
  `ID` int(11) NOT NULL,
  `DISPLAY` varchar(255) DEFAULT NULL,
  `VALUE` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `attributeunit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `attributeunit` (
  `ID` int(11) NOT NULL,
  `DISPLAY` varchar(255) DEFAULT NULL,
  `MULTIPLIER` decimal(38,0) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `bid`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `bid` (
  `ID` int(11) NOT NULL,
  `ACCOUNT` varchar(255) DEFAULT NULL,
  `BIDAMOUNT` decimal(10,2) DEFAULT NULL,
  `BIDDATE` datetime DEFAULT NULL,
  `ISBUYNOW` tinyint(1) DEFAULT '0',
  `ISBYMOBILE` tinyint(1) DEFAULT '0',
  `ISBYPROXY` tinyint(1) DEFAULT '0',
  `BIDDER_MEMBERID` int(11) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `FK_BID_BIDDER_MEMBERID` (`BIDDER_MEMBERID`),
  CONSTRAINT `FK_BID_BIDDER_MEMBERID` FOREIGN KEY (`BIDDER_MEMBERID`) REFERENCES `member` (`MEMBERID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `bidpages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `bidpages` (
  `ID` int(11) NOT NULL,
  `DTYPE` varchar(31) DEFAULT NULL,
  `PAGE` int(11) DEFAULT NULL,
  `PAGESIZE` int(11) DEFAULT NULL,
  `TOTALCOUNT` int(11) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `bidpages_bid`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `bidpages_bid` (
  `BidPages_ID` int(11) NOT NULL,
  `list_ID` int(11) NOT NULL,
  PRIMARY KEY (`BidPages_ID`,`list_ID`),
  KEY `FK_BIDPAGES_BID_list_ID` (`list_ID`),
  CONSTRAINT `FK_BIDPAGES_BID_list_ID` FOREIGN KEY (`list_ID`) REFERENCES `bid` (`ID`),
  CONSTRAINT `FK_BIDPAGES_BID_BidPages_ID` FOREIGN KEY (`BidPages_ID`) REFERENCES `bidpages` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `dealership`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dealership` (
  `ID` int(11) NOT NULL,
  `ADDRESS` varchar(255) DEFAULT NULL,
  `BILLINGTYPE` int(11) DEFAULT NULL,
  `CITY` varchar(255) DEFAULT NULL,
  `DEALERS` longblob,
  `EMAIL` varchar(255) DEFAULT NULL,
  `FAXNUMBER` varchar(255) DEFAULT NULL,
  `LOGO` varchar(255) DEFAULT NULL,
  `NAME` varchar(255) DEFAULT NULL,
  `PHONENUMBER` varchar(255) DEFAULT NULL,
  `SUBURB` varchar(255) DEFAULT NULL,
  `TYPE` int(11) DEFAULT NULL,
  `WEBSITE` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `geographiclocation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `geographiclocation` (
  `ID` int(11) NOT NULL,
  `ACCURACY` int(11) DEFAULT NULL,
  `EASTING` int(11) DEFAULT NULL,
  `LATITUDE` double DEFAULT NULL,
  `LONGITUDE` double DEFAULT NULL,
  `NORTHING` int(11) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `invalidlisting`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `invalidlisting` (
  `LISTINGID` int(11) NOT NULL,
  `LISTINGERROR` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`LISTINGID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `listeditemdetail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `listeditemdetail` (
  `LISTINGID` int(11) NOT NULL,
  `ALLOWSPICKUPS` varchar(255) DEFAULT NULL,
  `ASAT` datetime DEFAULT NULL,
  `AUTHENTICATEDMEMBERSONLY` tinyint(1) DEFAULT '0',
  `BIDCOUNT` int(11) DEFAULT NULL,
  `BIDDERANDWATCHERS` int(11) DEFAULT NULL,
  `BODY` varchar(2048) DEFAULT NULL,
  `BUYNOWPRICE` decimal(10,2) DEFAULT NULL,
  `CATEGORY` varchar(255) DEFAULT NULL,
  `CATEGORYNAME` varchar(255) DEFAULT NULL,
  `CATEGORYPATH` varchar(255) DEFAULT NULL,
  `ENDDATE` datetime DEFAULT NULL,
  `HASBUYNOW` tinyint(1) DEFAULT '0',
  `HASEXTRAPHOTOS` tinyint(1) DEFAULT '0',
  `HASGALLERY` tinyint(1) DEFAULT '0',
  `HASHOMEPAGEFEATURE` tinyint(1) DEFAULT '0',
  `HASMULTIPLE` tinyint(1) DEFAULT '0',
  `HASPAYNOW` tinyint(1) DEFAULT '0',
  `HASRESERVE` tinyint(1) DEFAULT '0',
  `HASSCHEDULEDENDDATE` tinyint(1) DEFAULT '0',
  `ISBOLD` tinyint(1) DEFAULT '0',
  `ISBUYNOWONLY` tinyint(1) DEFAULT '0',
  `ISCLASSIFIED` tinyint(1) DEFAULT '0',
  `ISFEATURED` tinyint(1) DEFAULT '0',
  `ISFLATSHIPPINGCHARGE` tinyint(1) DEFAULT '0',
  `ISHIGHLIGHTED` tinyint(1) DEFAULT '0',
  `ISNEW` tinyint(1) DEFAULT '0',
  `ISORNEAROFFER` tinyint(1) DEFAULT '0',
  `ISRESERVEMET` tinyint(1) DEFAULT '0',
  `LISTINGLENGTH` varchar(255) DEFAULT NULL,
  `MAXBIDAMOUNT` decimal(10,2) DEFAULT NULL,
  `MINIMUMNEXTBIDAMOUNT` decimal(38,0) DEFAULT NULL,
  `NOTE` varchar(255) DEFAULT NULL,
  `NOTEDATE` datetime DEFAULT NULL,
  `NOTEID` int(11) DEFAULT NULL,
  `PAYMENTOPTIONS` varchar(255) DEFAULT NULL,
  `PHOTOID` int(11) DEFAULT NULL,
  `PICTUREHREF` varchar(255) DEFAULT NULL,
  `POSITIVEREVIEWCOUNT` int(11) DEFAULT NULL,
  `PRICEDISPLAY` varchar(255) DEFAULT NULL,
  `REGION` varchar(255) DEFAULT NULL,
  `REGIONID` int(11) DEFAULT NULL,
  `RELISTEDITEMID` int(11) DEFAULT NULL,
  `RESERVEPRICE` decimal(10,2) DEFAULT NULL,
  `RESERVESTATE` varchar(255) DEFAULT NULL,
  `RESTRICTIONS` varchar(255) DEFAULT NULL,
  `STARTDATE` datetime DEFAULT NULL,
  `STARTPRICE` decimal(10,2) DEFAULT NULL,
  `SUBTITLE` varchar(255) DEFAULT NULL,
  `SUBURB` varchar(255) DEFAULT NULL,
  `SUBURBID` int(11) DEFAULT NULL,
  `TITLE` varchar(255) DEFAULT NULL,
  `TOTALREVIEWCOUNT` int(11) DEFAULT NULL,
  `UNANSWEREDQUESTIONCOUNT` int(11) DEFAULT NULL,
  `VIDEOURL` varchar(255) DEFAULT NULL,
  `VIEWCOUNT` int(11) DEFAULT NULL,
  `BESTCONTACTTIME` varchar(255) DEFAULT NULL,
  `CONTACTNAME` varchar(255) DEFAULT NULL,
  `MOBILEPHONENUMBER` varchar(255) DEFAULT NULL,
  `PHONENUMBER` varchar(255) DEFAULT NULL,
  `MEMBER_MEMBERID` int(11) DEFAULT NULL,
  `AGENCY_ID` int(11) DEFAULT NULL,
  `BIDS_ID` int(11) DEFAULT NULL,
  `DEALERSHIP_ID` int(11) DEFAULT NULL,
  `GEOGRAPHICLOCATION_ID` int(11) DEFAULT NULL,
  `QUESTIONS_ID` int(11) DEFAULT NULL,
  `SELLER_MEMBERID` int(11) DEFAULT NULL,
  PRIMARY KEY (`LISTINGID`),
  KEY `FK_LISTEDITEMDETAIL_GEOGRAPHICLOCATION_ID` (`GEOGRAPHICLOCATION_ID`),
  KEY `FK_LISTEDITEMDETAIL_AGENCY_ID` (`AGENCY_ID`),
  KEY `FK_LISTEDITEMDETAIL_QUESTIONS_ID` (`QUESTIONS_ID`),
  KEY `FK_LISTEDITEMDETAIL_BIDS_ID` (`BIDS_ID`),
  KEY `FK_LISTEDITEMDETAIL_SELLER_MEMBERID` (`SELLER_MEMBERID`),
  KEY `FK_LISTEDITEMDETAIL_DEALERSHIP_ID` (`DEALERSHIP_ID`),
  KEY `FK_LISTEDITEMDETAIL_MEMBER_MEMBERID` (`MEMBER_MEMBERID`),
  CONSTRAINT `FK_LISTEDITEMDETAIL_MEMBER_MEMBERID` FOREIGN KEY (`MEMBER_MEMBERID`) REFERENCES `member` (`MEMBERID`),
  CONSTRAINT `FK_LISTEDITEMDETAIL_AGENCY_ID` FOREIGN KEY (`AGENCY_ID`) REFERENCES `agency` (`ID`),
  CONSTRAINT `FK_LISTEDITEMDETAIL_BIDS_ID` FOREIGN KEY (`BIDS_ID`) REFERENCES `bidpages` (`ID`),
  CONSTRAINT `FK_LISTEDITEMDETAIL_DEALERSHIP_ID` FOREIGN KEY (`DEALERSHIP_ID`) REFERENCES `dealership` (`ID`),
  CONSTRAINT `FK_LISTEDITEMDETAIL_GEOGRAPHICLOCATION_ID` FOREIGN KEY (`GEOGRAPHICLOCATION_ID`) REFERENCES `geographiclocation` (`ID`),
  CONSTRAINT `FK_LISTEDITEMDETAIL_QUESTIONS_ID` FOREIGN KEY (`QUESTIONS_ID`) REFERENCES `questionpages` (`ID`),
  CONSTRAINT `FK_LISTEDITEMDETAIL_SELLER_MEMBERID` FOREIGN KEY (`SELLER_MEMBERID`) REFERENCES `member` (`MEMBERID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `listeditemdetail_attribute`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `listeditemdetail_attribute` (
  `ListedItemDetail_LISTINGID` int(11) NOT NULL,
  `attributes_ID` int(11) NOT NULL,
  PRIMARY KEY (`ListedItemDetail_LISTINGID`,`attributes_ID`),
  KEY `FK_LISTEDITEMDETAIL_ATTRIBUTE_attributes_ID` (`attributes_ID`),
  CONSTRAINT `LISTEDITEMDETAILATTRIBUTEListedItemDetailLISTINGID` FOREIGN KEY (`ListedItemDetail_LISTINGID`) REFERENCES `listeditemdetail` (`LISTINGID`),
  CONSTRAINT `FK_LISTEDITEMDETAIL_ATTRIBUTE_attributes_ID` FOREIGN KEY (`attributes_ID`) REFERENCES `attribute` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `listeditemdetail_openhome`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `listeditemdetail_openhome` (
  `ListedItemDetail_LISTINGID` int(11) NOT NULL,
  `openHomes_ID` int(11) NOT NULL,
  PRIMARY KEY (`ListedItemDetail_LISTINGID`,`openHomes_ID`),
  KEY `FK_LISTEDITEMDETAIL_OPENHOME_openHomes_ID` (`openHomes_ID`),
  CONSTRAINT `LISTEDITEMDETAILOPENHOMEListedItemDetail_LISTINGID` FOREIGN KEY (`ListedItemDetail_LISTINGID`) REFERENCES `listeditemdetail` (`LISTINGID`),
  CONSTRAINT `FK_LISTEDITEMDETAIL_OPENHOME_openHomes_ID` FOREIGN KEY (`openHomes_ID`) REFERENCES `openhome` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `listeditemdetail_photo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `listeditemdetail_photo` (
  `ListedItemDetail_LISTINGID` int(11) NOT NULL,
  `photos_PHOTOID` int(11) NOT NULL,
  PRIMARY KEY (`ListedItemDetail_LISTINGID`,`photos_PHOTOID`),
  KEY `FK_LISTEDITEMDETAIL_PHOTO_photos_PHOTOID` (`photos_PHOTOID`),
  CONSTRAINT `FK_LISTEDITEMDETAIL_PHOTO_photos_PHOTOID` FOREIGN KEY (`photos_PHOTOID`) REFERENCES `photo` (`PHOTOID`),
  CONSTRAINT `LISTEDITEMDETAIL_PHOTO_ListedItemDetail_LISTINGID` FOREIGN KEY (`ListedItemDetail_LISTINGID`) REFERENCES `listeditemdetail` (`LISTINGID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `listeditemdetail_shippingoption`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `listeditemdetail_shippingoption` (
  `ListedItemDetail_LISTINGID` int(11) NOT NULL,
  `shippingOptions_ID` int(11) NOT NULL,
  PRIMARY KEY (`ListedItemDetail_LISTINGID`,`shippingOptions_ID`),
  KEY `LISTEDITEMDETAIL_SHIPPINGOPTION_shippingOptions_ID` (`shippingOptions_ID`),
  CONSTRAINT `LISTEDITEMDETAIL_SHIPPINGOPTION_shippingOptions_ID` FOREIGN KEY (`shippingOptions_ID`) REFERENCES `shippingoption` (`ID`),
  CONSTRAINT `LSTDTEMDETAILSHIPPINGOPTIONLstdItemDetailLISTINGID` FOREIGN KEY (`ListedItemDetail_LISTINGID`) REFERENCES `listeditemdetail` (`LISTINGID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `member` (
  `MEMBERID` int(11) NOT NULL,
  `DATEADDRESSVERIFIED` datetime DEFAULT NULL,
  `DATEJOINED` datetime DEFAULT NULL,
  `EMAIL` varchar(255) DEFAULT NULL,
  `FEEDBACKCOUNT` int(11) DEFAULT NULL,
  `ISADDRESSVERIFIED` tinyint(1) DEFAULT '0',
  `ISAUTHENTICATED` tinyint(1) DEFAULT '0',
  `ISDEALER` tinyint(1) DEFAULT '0',
  `NICKNAME` varchar(255) DEFAULT NULL,
  `REGION` varchar(255) DEFAULT NULL,
  `REGIONID` int(11) DEFAULT NULL,
  `SUBURB` varchar(255) DEFAULT NULL,
  `SUBURBID` int(11) DEFAULT NULL,
  `UNIQUENEGATIVE` int(11) DEFAULT NULL,
  `UNIQUEPOSITIVE` int(11) DEFAULT NULL,
  PRIMARY KEY (`MEMBERID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `openhome`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `openhome` (
  `ID` int(11) NOT NULL,
  `END` datetime DEFAULT NULL,
  `START` datetime DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `photo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `photo` (
  `PHOTOID` int(11) NOT NULL,
  `FULLSIZE` varchar(255) DEFAULT NULL,
  `GALLERY` varchar(255) DEFAULT NULL,
  `LARGE` varchar(255) DEFAULT NULL,
  `LIST` varchar(255) DEFAULT NULL,
  `MEDIUM` varchar(255) DEFAULT NULL,
  `URL_PHOTOID` int(11) DEFAULT NULL,
  `THUMBNAIL` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`PHOTOID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `question`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `question` (
  `LISTINGQUESTIONID` int(11) NOT NULL,
  `ANSWER` varchar(500) DEFAULT NULL,
  `ANSWERDATE` datetime DEFAULT NULL,
  `COMMENT` varchar(500) DEFAULT NULL,
  `COMMENTDATE` datetime DEFAULT NULL,
  `ISSELLERCOMMENT` tinyint(1) DEFAULT '0',
  `LISTINGID` int(11) DEFAULT NULL,
  `ASKINGMEMBER_MEMBERID` int(11) DEFAULT NULL,
  PRIMARY KEY (`LISTINGQUESTIONID`),
  KEY `FK_QUESTION_ASKINGMEMBER_MEMBERID` (`ASKINGMEMBER_MEMBERID`),
  CONSTRAINT `FK_QUESTION_ASKINGMEMBER_MEMBERID` FOREIGN KEY (`ASKINGMEMBER_MEMBERID`) REFERENCES `member` (`MEMBERID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `questionpages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `questionpages` (
  `ID` int(11) NOT NULL,
  `DTYPE` varchar(31) DEFAULT NULL,
  `PAGE` int(11) DEFAULT NULL,
  `PAGESIZE` int(11) DEFAULT NULL,
  `TOTALCOUNT` int(11) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `questionpages_question`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `questionpages_question` (
  `QuestionPages_ID` int(11) NOT NULL,
  `list_LISTINGQUESTIONID` int(11) NOT NULL,
  PRIMARY KEY (`QuestionPages_ID`,`list_LISTINGQUESTIONID`),
  KEY `FK_QUESTIONPAGES_QUESTION_list_LISTINGQUESTIONID` (`list_LISTINGQUESTIONID`),
  CONSTRAINT `FK_QUESTIONPAGES_QUESTION_QuestionPages_ID` FOREIGN KEY (`QuestionPages_ID`) REFERENCES `questionpages` (`ID`),
  CONSTRAINT `FK_QUESTIONPAGES_QUESTION_list_LISTINGQUESTIONID` FOREIGN KEY (`list_LISTINGQUESTIONID`) REFERENCES `question` (`LISTINGQUESTIONID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sequence`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sequence` (
  `SEQ_NAME` varchar(50) NOT NULL,
  `SEQ_COUNT` decimal(38,0) DEFAULT NULL,
  PRIMARY KEY (`SEQ_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `shippingoption`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shippingoption` (
  `ID` int(11) NOT NULL,
  `METHOD` varchar(255) DEFAULT NULL,
  `PRICE` decimal(38,0) DEFAULT NULL,
  `SHIPPINGID` int(11) DEFAULT NULL,
  `TYPE` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

