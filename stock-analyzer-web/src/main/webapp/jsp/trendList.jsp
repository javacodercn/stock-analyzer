<%@ page language="java"  pageEncoding="UTF-8"%>
<%@ page isELIgnored ="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>


<ul>
    <c:forEach  items="${list}" var="item">
        <li><c:out value="${item}"/></li>
    </c:forEach>
</ul>
