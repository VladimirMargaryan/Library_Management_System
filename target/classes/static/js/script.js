let box  = document.getElementById('box');
let down = false;


function toggleNotify(){
    if (down) {
        box.style.height  = '0px';
        box.style.opacity = '0';
        // box.style.display = 'none';
        down = false;
    }else {
        box.style.height  = '510px';
        box.style.opacity = '1';
        // box.style.display = 'inline-block';
        down = true;
    }
}