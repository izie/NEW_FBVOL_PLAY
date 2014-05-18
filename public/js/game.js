var i = 0;

var gWidth = 800;
var gHeight = 600;

var userData = {};

var kimages = {};

var profile_pic = {
    dkjo91: 'https://fbcdn-profile-a.akamaihd.net/hprofile-ak-frc3/t1.0-1/c35.35.443.443/s160x160/484064_384552564940063_232438685_n.jpg',
    tkdxo0624: 'https://fbcdn-profile-a.akamaihd.net/hprofile-ak-prn2/t1.0-1/p160x160/10256603_833148846699423_3550347983526187_n.jpg'
};

var bg_pic = "http://wsp.wooribank.com/download/0008830890_041/0008830890_18467.JPG";
var bgimage = null;

var stage = null;

var layer = null;
// Ball
var ball = null;

//Fence
var fence = null;

// Ground
var ground = null;

// ScoreBoard
var scoreBoard_1 = null;
var score_A = 0;
var scoreBoard_2 = null;
var score_B = 0;

var ballMode = 'move';

// Gravity
var grav_x = 0;
var grav_y = 9;

// Start Point
var start_x;
var start_y;

// BAll Speed
var speed_x;
var speed_y;

// Character speed_y
var userSpeed_y = {};

// Character is shooting
var userShoot = {};

var debugTxt = null;

var numUser = 0;


/*var timer = setInterval(function () {
 //alert('Timer Start');
 //clearInterval(timer);

 $.callAjax();
 }, 1000);*/

$.Init = function() {
    $.initStage();
    $.initRes();
    $.initVar();
    $.initAnim();
    //$('#display_progress .progress-bar' ).css('width','90%');
    //$.gameStart();

}
/*$('#container').keydown(function (event){
 if(event.)

 });*/

$.initStage = function() {
    stage = new Kinetic.Stage({
        container: 'container',
        width: gWidth,
        height: gHeight
    });

    layer = new Kinetic.Layer();
}

$.initRes = function() {
    // Ball
    ball = new Kinetic.Circle({
        x: 150,
        y: 50,
        radius: 30,
        fill: 'red',
        stroke: 'black',
        strokeWidth: 1
    });

    ball.setOffset(100, 30);

    // Fence
    fence = new Kinetic.Rect({
        x:stage.getWidth() / 2,
        y:400,
        width:10,
        height:200,
        fill:'blue',
        stroke: 'black',
        strokeWidth: 4
    });

    // Ground
    ground = new Kinetic.Rect({
        x:0,
        y:595,
        width:800,
        height:5,
        fill:'grey',
        strokeWidth: 0
    });

    // ScoreBoard
    scoreBoard_1 = new Kinetic.Text({
        x:200,
        y:30,
        text: '0',
        fontSize: 30,
        fontFamily: 'Calibri',
        fill: 'white'
    });

    // ScoreBoard
    scoreBoard_2 = new Kinetic.Text({
        x:700,
        y:30,
        text: '0',
        fontSize: 30,
        fontFamily: 'Calibri',
        fill: 'white'
    });

    // Debug Text
    debugTxt = new Kinetic.Text({
        x: 0,
        y: 15,
        text: 'Debug',
        fontSize: 10,
        fontFamily: 'Calibri',
        fill: 'green'
    });

    bgimage = new Image();

    var imageObj = new Image();
    imageObj.onload = function() {
        var yoda = new Kinetic.Image({
            x: 0,
            y: 0,
            image: imageObj,
            width: 800,
            height: 600
        });

        // add the shape to the layer
        layer.add(yoda);
        layer.add(debugTxt);


        // add the shape to the layer
        layer.add(ball);
        layer.add(fence);
        layer.add(scoreBoard_1);
        layer.add(scoreBoard_2);
        layer.add(ground);

        stage.add(layer);
    };
    imageObj.src = bg_pic;


}

$.initVar = function() {
    speed_x = 0;
    speed_y = 0;
}

$.setUserImage = function(seq, pic_url) {

}

$.callAjax = function() {
    var requestUrl = 'http://fbvol.matthewlab.com/User/getUser';

    $.ajax({
        url:requestUrl,
        type:'GET',
        error: function(){
            console.log("Ajax Loading Error");
        },
        success: function(data){
            var images = {};
            var k = 0;
            //var results = $.parseJSON(data);
            for(var i = 0 ; i < data.length ; i++){
                var oUser = data[i];

                if(numUser != 0){
                    kimages[i].setX(oUser.x);
                    kimages[i].setY(oUser.y);
                    kimages[i].setOffset(40, 40);
                }else{
                    //$.addImg(oUser.x,oUser.y,oUser.id);
                    images[oUser.id] = new Image();

                    images[oUser.id].onload = function() {
                        //alert(images[numUser].src);
                        kimages[numUser] = new Kinetic.Image({
                            image: images[data[numUser].id],
                            x: data[numUser].x,
                            y: data[numUser].y,
                            width:80,
                            height:80,
                            draggable: true
                        });

                        kimages[numUser].setOffset(40, 40);

                        //alert(data[numUser].id+"/"+data[numUser].x+"/"+data[numUser].y);
                        layer.add(kimages[numUser]);

                        numUser++;
                        stage.add(layer);
                    };

                    images[oUser.id].src = profile_pic[oUser.id];
                }

            }

            // finally, we need to redraw the layer hit graph
            layer.drawHit();

            userData = data;

        }

    });
};

$.boundaryCheck = function(){

    if(ball.getX() <=30){
        if(speed_x < 0)
            speed_x = speed_x * -1;

    }
    if(ball.getX() >= 770){
        if(speed_x > 0)
            speed_x = speed_x * -1;

    }
    if(ball.getY() <= 30){
        if(speed_y < 0)
            speed_y = speed_y * -1;

    }
}

$.calculateScore = function(){
    //debugTxt.setText("A : "+score_A+"B : "+score_B);
    if(ball.getY() > gHeight){
        //ballMode = 'stop';
        if(ball.getX() < 400){
            ball.setX(650);
            ball.setY(70);
            score_B++;
            scoreBoard_2.setText(score_B);
        }else{
            ball.setX(150);
            ball.setY(70);
            score_A++;
            scoreBoard_1.setText(score_A);
        }

        kimages[0].setX(110);             //서버에서 날아오는 이전의 캐릭터 위치로 다시 돌아감!!
        kimages[0].setY(500);

        kimages[1].setX(610);
        kimages[1].setY(500);

        speed_x = 0;
        speed_y = 0;

    }


}
$.jumpAction = function(i){
    if(userSpeed_y[i] == 0 ){
        userSpeed_y [i] = -40;
    }
}

$.startShootingAction = function(i){
    userShoot[i] = 1;

}
$.endShootingAction = function(i){
    userShoot[i] = 0;
}

$.initAnim = function() {
    var firstTime = 0;

    anim = new Kinetic.Animation(function(frame){
        //console.log((frame.time / 1000) - firstTime);
        if((frame.time / 35) - firstTime >= 1){
            //console.log("check");
            firstTime = frame.time / 35;

        }
        if (ballMode == 'move'){                        // 상황별로 나누기, 지금은 그냥 공움직이게 되어잇음.
            $.callAjax();
            var tmod = (frame.timeDiff) * 0.005;

            // 속도 만큼 공을 움직인다.
            ball.setX(ball.getX() + (speed_x * tmod));
            ball.setY(ball.getY() + (speed_y * tmod));

            // 속도가 중력가속도의 영향을 받는다.
            speed_y += grav_y * tmod;


            for(var i = 0 ; i< numUser; i++ ){

                if(  kimages[i].getY() < 500){
                    kimages[i].setY(kimages[i].getY() + (userSpeed_y[i] * tmod));
                    userSpeed_y[i] += grav_y * tmod;
                }
                else{
                    userSpeed_y[i] = 0
                }
            }

            $.boundaryCheck();
            $.calculateScore();

            $.isBallTouched(ball.getX(),ball.getY()); // 사람체크
            $.isCollisionToNet(ball.getX(),ball.getY()); // 울타리체크
        }
        else if (ballMode == 'pause'){
            // 시간 멈추기.
        }
        else if( ballMode =='ready' ){
            kimages[0].setX(110);
            kimages[0].setY(500);

            kimages[1].setX(610);
            kimages[1].setY(500);

            ball.setX(150);
            ball.setY(70);

            speed_x = 0;
            speed_y = 0;
        }

    },layer);
}

$.isBallTouched = function(x1,y1){

    //debugTxt.setText("kimages : "+numUser);
    for(i = 0 ; i < numUser ; i++){
        var character_x =  kimages[i].getX() +40;
        var character_y =  kimages[i].getY() +40;

        var distance = (x1-character_x)*(x1-character_x) + (y1 - character_y) * (y1 - character_y);

        if(distance < 4900){
            if(userShoot[i] == 1){
                speed_y = -3;
                speed_x = -(x1 - 400) * 1.3;
            }
            else{
                speed_y = -50;
                speed_x = (x1 - (kimages[i].getX()+40)) * 1.3;
                //speed_y = -3;
                //speed_x = -(x1 - 400) * 1.3;
            }
        }
        else{}
    }

}

$.isCollisionToNet = function(x1,y1){

    var x2 = fence.getX() ;
    var x3 = fence.getX() +10;

    var y2 = fence.getY() ;
    var y3 = fence.getY() + 200;

    if( (y1 > y2 -30) && ((x2 <= x1+30 ) &&(x1+30 <= x2 +10)) && (speed_x > 0)){  // 네트 왼쪽 충돌
        debugTxt.setText("collision!  옆면");
        speed_x = speed_x * -0.9;

    }else if((y1 > y2 -30) &&((x3-10 <= x1-30)&& (x3 >= x1-30 )) &&(speed_x < 0) ){ // 네트 오른쪽 충돌
        debugTxt.setText("collision!  옆면");
        speed_x = speed_x * -0.9;

    }else if( (( y2 <=  y1+30) && ( y1+30 <= y2+5 )) && ((x2 <= x1)&&(x1 <= x3))  ){ // 네트 위 충돌
        speed_y = speed_y * -0.9;

    }else{}

}

$.gameStart = function() {
    anim.start();
}

$.gamePause = function() {
    anim.pause();
}

$.Init();