<!DOCTYPE html>
<html>
<head>     
    <meta charset="utf‐8">     
    <title>Hello World!</title>
</head>
<body>
Hello ${name}!

<h2>用FTL指令遍历list中的数据=========</h2>

<table>     
    <tr>     
        <td>序号</td>              
        <td>姓名</td>         
        <td>年龄</td>         
        <td>钱包</td>  
        <td>生日</td>   
    </tr>     
    <#list stus as stu>         
        <tr>             
            <td>${stu_index + 1}</td>             
            <td <#if stu.name=='小明'>style="background: red" </#if>>${stu.name}</td>             
            <td>${stu.age}</td>             
            <td <#if stu.money gt 200>style="background: blue"</#if>>${stu.money}</td>
            <td>${stu.birthday?date}   //内建函数
        </tr>     
    </#list>  
</table>

<h2>遍历map数据===================</h2>
输出stu1的学生信息：<br/>
姓名：${stuMap['stu1'].name}<br/>
年龄：${stuMap['stu1'].age}<br/>
输出stu1的学生信息：<br/>
姓名：${stuMap.stu1.name}<br/>
年龄：${stuMap.stu1.age}<br/>
遍历输出两个学生信息：<br/>
<table>     
    <tr>         
        <td>序号</td>         
        <td>姓名</td>         
        <td>年龄</td>         
        <td>钱包</td>     
    </tr>
    <#list stuMap?keys as k>
        <tr>     
            <td>${k_index + 1}</td>     
            <td>${stuMap[k].name}</td>     
            <td>${stuMap[k].age}</td>     
            <td>${stuMap[k].money}</td>
        </tr>
    </#list>
</table>

<br/>
<#assign text="{'bank':'工商银行','account':'10101920201920212'}"/>
<#assign data=text?eval />
开户行：${data.bank}  账号：${data.account}
</body>
</html>