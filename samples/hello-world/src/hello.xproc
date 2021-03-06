<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:pkg="http://expath.org/ns/pkg"
                xmlns:web="http://expath.org/ns/webapp"
                pkg:import-uri="http://expath.org/ns/samples/servlex/hello.xproc"
                name="pipeline"
                version="1.0">

   <p:input port="source"/>

   <p:output port="result">
      <p:pipe port="result" step="body"/>
   </p:output>

   <p:xslt name="body">
      <p:input port="source">
         <p:pipe step="pipeline" port="source"/>
      </p:input>
      <p:input port="stylesheet">
         <p:inline>
            <xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                            version="2.0">
               <xsl:template match="web:request">
                  <web:wrapper>
                     <web:response status="200" message="Ok">
                        <web:body content-type="application/xhtml+xml" method="xhtml"/>
                     </web:response>
                     <xsl:variable name="who" select="web:param[@name eq 'who']/@value"/>
                     <xsl:variable name="greetings" select="concat('Hello, ', $who, '!')"/>
                     <html xmlns="http://www.w3.org/1999/xhtml">
                        <head>
                           <title>
                              <xsl:value-of select="$greetings"/>
                           </title>
                        </head>
                        <body>
                           <p>
                              <xsl:value-of select="$greetings"/>
                              <xsl:text> (in XProc)</xsl:text>
                           </p>
                        </body>
                     </html>
                  </web:wrapper>
               </xsl:template>
            </xsl:stylesheet>
         </p:inline>
      </p:input>
      <p:with-param name="who" select="web:param[@name eq 'who']/@value">
         <p:pipe step="pipeline" port="source"/>
      </p:with-param>
   </p:xslt>

</p:declare-step>
