<?xml version="1.0" encoding="UTF-8" ?>

<!-- the 2.0 version of xsl reqires a custom processor to be used. Saxon9he is used, and is
located in Jetty's ext/ folder. This library requires Jetty to be started like so:
    java -Djavax.xml.transform.TransformerFactory=net.sf.saxon.TransformerFactoryImpl -jar start.jar
    -->
<xsl:stylesheet version="2.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:fn="http://www.w3.org/2005/xpath-functions"
	xmlns:xdt="http://www.w3.org/2005/xpath-datatypes"
	xmlns:err="http://www.w3.org/2005/xqt-errors"
	xmlns:tm="http://api.trademe.co.nz/v1"
		exclude-result-prefixes="xs xdt err fn tm">

    <xsl:output method="xml" indent="yes"/>

    <!-- 'Auction" is the root XML element -->
    <xsl:template match="tm:Auction">
        <add><doc>
            <xsl:for-each select="//text()/.. intersect child::*">
                <field>
                    <xsl:attribute name="name">
                        <xsl:value-of select="name()"/>
                    </xsl:attribute>
                    <xsl:value-of select="."/>
                </field>
            </xsl:for-each>

            <xsl:for-each select="//text()/.. except child::*">
                <field>
                    <xsl:attribute name="name">
                        <xsl:value-of select="../name()"/>_<xsl:value-of select="name()"/>
                    </xsl:attribute>
                    <xsl:value-of select="."/>
                </field>
            </xsl:for-each>
        </doc></add>
    </xsl:template>
</xsl:stylesheet>
