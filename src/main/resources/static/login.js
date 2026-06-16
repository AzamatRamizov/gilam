$(document).ready(function (){
    $("#submitbtn").click(function (){
        login()
    })
})
function login(){
    var loginDto={}
    loginDto.login=$("#login").val()
    loginDto.password=$("#password").val()
    $.ajax({
        url:"/api/auth/login",
        type:"POST",
        data:JSON.stringify(loginDto),
        contentType:"application/json;charset=utf-8",
        success:function (habar,ogoh,sts){
            console.log(habar);
            // Tokenni cookie ga yozamiz
            document.cookie = "Auth=" + habar.token + "; path=/; SameSite=Lax"; // HttpOnly bo'lsa backendda o'rnatiladi
            // Dashboardga yo'naltiramiz
            document.location.href = "/admin/dashboard";
        },
        error:function (xato){
            console.log(xato)
        }
    })
}