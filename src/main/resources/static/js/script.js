for (let i = 0; i < document.links.length; i++) {
    if (document.links[i].href === document.URL) {
        document.links[i].className = 'active';
    }
}

function Validate() {
    let password = document.getElementById("password").value;
    let confirmPassword = document.getElementById("confirm_password").value;
    if (password !== confirmPassword) {
        alert("Passwords do not match.");
        return false;
    }
    return true;
}

document.getElementById("optionsRadios1").setAttribute("checked","checked")
document.getElementById("optionsRadios2").setAttribute("checked","checked")
function addField(n) {
    let tr = n.parentNode.parentNode.cloneNode(true);
    document.getElementById('tbl').appendChild(tr);
}


function popup(myLink, windowName) {
    if (!window.focus)
        return true;
    let href;
    if (typeof(myLink) == 'string')
        href = myLink;
    else href = myLink.href;
    window.open(href, windowName + (Math.random() + 1).toString(36).substring(7), 'width=700,height=500,scrollbars=yes');
    return false;
}
