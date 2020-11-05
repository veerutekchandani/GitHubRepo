<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Almabase project</title>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.4.1/css/bootstrap.min.css"/>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.16.0/umd/popper.min.js"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.4.1/js/bootstrap.min.js"></script>
</head>
<style>
    input {
        border: 1px solid black;
        border-radius: 5px;
    }
    a {
        color:black;
        float:left;
    }
</style>
<body style="height: 100vh;overflow-x: hidden;background: rgba(0,0,0,0.09);">
<div style="margin-top: 20px;">
    <h4 align="center">Find top n github repositories with top m contributors</h4>
    <div class="row">
        <div class="col-md-1"></div>
        <div class="col-md-3" align="center"><br><br>
            <form:form method="POST" action="/submit" modelAttribute="repo">
                <table style="border-collapse:separate; border-spacing:5px;">
                    <tr>
                        <td><form:label path="userName">UserName:</form:label></td>
                        <td><form:input path="userName" placeholder="Enter username" required="required"></form:input></td>
                    </tr>
                    <tr>
                        <td><form:label path="n">Value of n:</form:label></td>
                        <td><form:input cssStyle="width: 100%;" type="number" path="n" placeholder="Enter n" required="required" min="1" max="2000"></form:input></td>
                    </tr>
                    <tr>
                        <td><form:label path="m">Value of m:</form:label></td>
                        <td><form:input cssStyle="width: 100%;" type="number" path="m" placeholder="Enter m" required="required" min="1" max="2000"></form:input></td>
                    </tr>
                    <tr>
                        <td></td>
                        <td><input type="submit" value="Submit" class="btn btn-success"></td>
                    </tr>
                </table>
            </form:form>
            <br><br>
            <c:if test="${(not empty userName) and (empty error)}">
                <a class="btn btn-primary" href="https://github.com/${userName}">${userName}</a>
            </c:if>
        </div>

        <div class="col-md-6" align="center" style="color:black;"><br><br>
            <div style="overflow-y: scroll;max-height:500px;">
                <c:if test="${not empty error}">
                    Error: ${error}
                </c:if>
                <c:set var="count" value="1" scope="page" />
                <c:set var="baseUrl" value="https://github.com/" />
                <c:forEach items="${repoList}" var="list">
                    <ul class="list-group" style="line-height: 15px;cursor:pointer;">
                        <li style="background: #99ccff;" class="list-group-item" data-toggle="collapse" data-target="#collapse${count}">
                            <a href="${baseUrl}${userName}/${list.key}"><c:out value="${list.key}"></c:out></a>
                            <span style="float: right;">+</span></li>
                        <ul id="collapse${count}" class="list-group collapse" style="line-height: 10px;cursor:default;">
                        <c:forEach items="${list.value}" var="listItem">
                            <li style="background: #ADEBAD;" class="list-group-item">
                                <a style="padding-left: 20px;" href="${baseUrl}${listItem}"><c:out value="${listItem}"></c:out></a>
                            </li>
                        </c:forEach>
                        <c:set var="count" value="${count + 1}" scope="page"/>
                    </ul>
                </c:forEach>
            </div>
        </div>
        <div class="col-md-2"></div>
    </div>
</div>
</body>
</html>
