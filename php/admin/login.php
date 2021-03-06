<?php

require_once "WEB-INF/php/inc.php";

$title = "Resin Admin Login";

display_header("login.php", $title, null);

if ($error) {
  echo "<h3 class='fail'>Error: $error</h3>";
}

?>
<h2>Login</h2>

<form method="POST" action="j_security_check?j_uri=index.php">
<table border='0'>
<tr>
  <th>Username: </th>
  <td>
    <input name="j_username" width="40"></input>
  </td>
</tr>
<tr>
  <th>Password: </th>
  <td>
    <input name="j_password" width="40" type="password"></input>
  </td>
</tr>
<tr>
  <td>
    <input type="submit"></input>
  </td>
  <td></td>
</tr>
</table>
</form>

<?php

$digest_username = null;

include "digest.php";

if (! empty($digest)) {
  admin_init();
  $conf_dir = dirname($g_resin->getConfigFile());
  $password_file = realpath("{$conf_dir}/admin-users.xml.generated");
  // generate temporary config file
  $file = fopen($password_file, "w");

  fwrite($file, <<<EOF
<!-- Automatically generated by Resin Admin -->
<!-- move to {$conf_dir}/admin-users.xml to install -->
<resin:AdminAuthenticator xmlns="http://caucho.com/ns/resin"
                          xmlns:resin="urn:java:com.caucho.resin">
  <user name="$digest_username" password="$digest"/>
</resin:AdminAuthenticator>
EOF
);
  fclose($file);
?>

<ol>
<li>
A login configuration file has been written under the name 
<code>admin-users.xml.generated</code> in the same directory as your
resin.xml file. Simply rename this file to <code>admin-users.xml</code>
to install your login.
</li>

<li>Or cut and paste the following into the <code>admin-users.xml</code>

<pre>
  &lt;resin:AdminAuthenticator xmlns="http://caucho.com/ns/resin"
                            xmlns:resin="urn:java:com.caucho.resin">
      &lt;user name="<?= $digest_username ?>" password="<?= $digest ?>"/>
  &lt;/resin:AdminAuthenticator>
</pre>
</li>

<li>
By default, access to the administration application is limited
to the localhost.  The default behaviour can be changed in the 
resin.xml file.  To enable access to clients other than localhost:

<pre>
  &lt;resin:set var="resin_admin_external" value="true"/&gt;
</pre>
</li>

<li>
Once the file has been saved, you can
<a href="<?= $login_uri ?>">continue to the administration area</a>.
This will trigger a server restart, so just refresh your browser
until you see the login page again.
</li>

<li>
When prompted, use the username and password you provided.
</li>
</ol>

<?php
}

  display_footer("login.php");
?>
