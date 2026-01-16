<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>
<div class="travel-index-nav">
    <div class="citylistbox">
        <#if model??>
        <#list model as item>
        <div class="listbox">
        <div class="list">
            <dl><dt>${item.name}</dt></dl>
        </div>
        <#if (item.mapValue)??>
        <div class="box">
        <ul class="mod-nav__side-list">
            <#list item.mapValue.children as firstNode>
                <#list firstNode.children as secondNode>
                    <li class="mod-nav__side-li" jump-through="1">
                        <h5 class="mod-nav__link-nav-second"><a href="https://ke.qq.com/course/list?mt=1001&amp;st=2001" title="互联网产品"
                                                                class="mod-nav__link-nav-second-link" target="_blank"
                                                                report-tdw="action=click&amp;obj1=second_level&amp;obj2=2001"
                                                                jump-start="title_second">${secondNode.name}</a></h5>
                        <div class="mod-nav__wrap-nav-third">
                            <#list secondNode.children as thirdNode>
                                <a href="course_list.html"
                                   class="mod-nav__link-nav-third mod-nav__wrap-nav-third_line" title="${thirdNode.name}" target="_blank"
                                   report-tdw="action=click&amp;obj1=third_level&amp;obj2=3001" jump-start="title_third" jump-through="1">${thirdNode.name}
                                </a>
                            </#list>
                        </div>
                    </li>
                </#list>
            </#list>
        </ul>
        </div>
        </#if>
        </div>
        </#list>
        </#if>
    </div>
</div>
</body>