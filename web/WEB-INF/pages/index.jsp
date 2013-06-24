<%@ page session="false"%>

<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%> 

<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <title>Reddit for Glass</title>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <meta name="description" content="">
  <meta name="author" content="">

	<!--link rel="stylesheet/less" href="less/bootstrap.less" type="text/css" /-->
	<!--link rel="stylesheet/less" href="less/responsive.less" type="text/css" /-->
	<!--script src="js/less-1.3.3.min.js"></script-->
	<!--append ‘#!watch’ to the browser URL, then refresh the page. -->
	
	<link href="css/bootstrap.min.css" rel="stylesheet">
	<link href="css/bootstrap-responsive.min.css" rel="stylesheet">
	<link href="css/style.css" rel="stylesheet">

  <!-- HTML5 shim, for IE6-8 support of HTML5 elements -->
  <!--[if lt IE 9]>
    <script src="js/html5shiv.js"></script>
  <![endif]-->

  <!-- Fav and touch icons -->
  <link rel="apple-touch-icon-precomposed" sizes="144x144" href="img/apple-touch-icon-144-precomposed.png">
  <link rel="apple-touch-icon-precomposed" sizes="114x114" href="img/apple-touch-icon-114-precomposed.png">
  <link rel="apple-touch-icon-precomposed" sizes="72x72" href="img/apple-touch-icon-72-precomposed.png">
  <link rel="apple-touch-icon-precomposed" href="img/apple-touch-icon-57-precomposed.png">
  <link rel="shortcut icon" href="img/favicon.png">
  
	<script type="text/javascript" src="js/jquery.min.js"></script>
	<script type="text/javascript" src="js/bootstrap.min.js"></script>
	<script type="text/javascript" src="js/scripts.js"></script>
	
	<style>
		ol li {
			margin-bottom: 10px !important;
		}
	</style>
</head>

<body>
<div class="container-fluid">
	<div class="row-fluid">
		<div class="span12">
			<div class="hero-unit" style="padding: 30px !important">
				<h1>
					Welcome to Reddit for Glass!
				</h1>
			</div>
			<div class="row-fluid">
				<div class="span5">
					<p class="lead">
						Using this service requires 3 simple steps:
					</p>
					<ol class="lead" >
						<li>
							<c:if test="${googleLoggedIn}">
								<a href="#" class="btn btn-large btn-primary disabled">Login into Google</a> Welcome ${googleEmail} (<a href="${googleLogoutRedirect}">logout</a>)
							</c:if>
							
						    <c:if test="${!googleLoggedIn}">
								<a href="${googleLoginRedirect}" class="btn btn-large btn-primary">Login into Google</a>
							</c:if>
						</li>
						<li>
							<c:if test="${googleOauthCredentialed || !googleLoggedIn}">
								<a href="#" class="btn btn-large btn-primary disabled">Authorize Glass</a>
							</c:if>
							
						    <c:if test="${!googleOauthCredentialed && googleLoggedIn}">
								<a href="/authorize-google" class="btn btn-large btn-primary">Authorize Glass</a>
							</c:if>
						</li>
						<li>

							<c:if test="${redditOauthCredentialed || !googleLoggedIn}">
								<a href="#" class="btn btn-large btn-primary disabled">Authorize Reddit</a>
							</c:if>
							
						    <c:if test="${!redditOauthCredentialed && googleLoggedIn}">
								<a href="/authorize-reddit" class="btn btn-large btn-primary">Authorize Reddit</a>
							</c:if>
						</li>
					</ol>
				</div>
				<div class="span7">
				<c:if test="${redditOauthCredentialed}">
					<table class="table table-striped">  
				        <thead>  
				          <tr>  
				            <th>Picture</th>  
				            <th>Title</th>  
				          </tr>  
				        </thead>  
				        <tbody>  
				          
				          <c:forEach items="${redditArticles.data.children}" var="article">
				            <c:if test="${article.data.thumbnail != null && article.data.thumbnail != 'default' && fn:contains(article.data.url, 'jpg')}">   
					          	<tr>  
					          		<td><button class="btn btn-primary" type="button" onclick="send('${article.data.id}')">Send</button></td>
						            <td><img src="${article.data.thumbnail}" class="img-rounded"></td>  
						            <td>${article.data.title}</td>  
					          	</tr>           
  							</c:if>        
						  </c:forEach>  
				        </tbody>  
				 	</table>  
				 </c:if>
				</div>
			</div>
		</div>
	</div>
</div>
</body>

<script>
  function send(articleId) {
		
	  $.ajax({
	        url: "/sendArticle",
	        type: "post",
	        data: articleId,
	        success: function (dataCheck) {
	        	if (dataCheck == 'Complete')
	        		alert("Successfully Sent");
	        }
	   });
  }
</script>
</html>
