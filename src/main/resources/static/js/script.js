let box  = document.getElementById('box');
let down = false;


function toggleNotify(){
    if (down) {
        box.style.height  = '0px';
        box.style.opacity = '0';
        down = false;
    }else {
        box.style.height  = '510px';
        box.style.opacity = '1';
        down = true;
    }
}