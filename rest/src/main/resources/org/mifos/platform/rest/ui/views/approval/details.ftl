[#ftl]
<div><span><b>State :</b></span><span>${approval.state}</span></div>

<div><span><b>Type :</b></span><span>${approval.type}</span></div>

<div><span><b>Operation :</b></span><span>${approval.operation}</span></div>

<div><span><b>Created Date :</b></span><span>${approval.printableCreatedOnDate}</span></div>

<div><span><b>Created By :</b></span><span>${createdBy}</span></div>


<div><span><b>Operation Arguments :</b></span><span>
[#assign i = 0]
[#list approval.approvalMethod.argsHolder.values as value]
    [#if !approvedBy??]
   	    <input id='value_${i}' type=text value=${value} />
    [#else]
        ${value}&nbsp;
    [/#if]
       [#assign i=i+1]
[/#list]
</span></div>

[#if !approvedBy??]
    <div><input onclick='updateArgs(${approval.id})' type=button value=Update /></div>
[/#if]

[#if approvedBy??]
    <div><span><b>Approved/Rejected Date :</b></span><span>${approval.printableApprovedOnDate}</span></div>
    <div><span><b>Approved/Rejected By :</b></span><span>${approvedBy}</span></div>
[/#if]

<div id='methodContent'>${approval.methodContent}<div>
