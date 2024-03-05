<%@ page contentType="text/html;charset=UTF-8"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<html>
<body>
<h3> ShortLinkMaker </h3><br>
<form:form action="create" method="post" modelAttribute="link">
    <p>Введите длинную ссылку:</p> <form:input path="longLink"/><br>
    <p>Желаемое короткое имя (если будет свободно):</p> <form:input path="endOfShortLink"/><br>
    <p>Как долго будет действительна, в днях (необязательное поле):</p> <form:input path="ttl"/><br>
    <p><input type="submit" value="Get short link!"></p>
</form:form>
</body>
</html>

