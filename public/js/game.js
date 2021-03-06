/*
 - 공 충돌시 튕기는 값 조정     done
 - 슈팅, 점프 테스트        done
 - 슈팅 방향 별 값 조정  nop
 - 점수 디비연결    nop
 - 점수 선택해서 승패 판정 -> 디비연결
 - 일시정지, 기권 등 세부 기능     nop
 - 서버에서 사용자 움직임 범위 제한
 - 코트 한쪽에서 3번 터치    안해
 */



var i = 0;

var gWidth = 800;
var gHeight = 600;

var userData = {};
var anim = null;
var anim_countdown = null;
var user1_Shoot = null;
var user2_Shoot = null;
var delay = null;
var doubleNet = null;

var kimages = {};
var userInfo = {};

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


//CountDown
var countDownField = null;

var ballMode = 'ready';

// Gravity
var grav_y = 15;               // 중력이랑 와이축방향 점프값 조정!!!
var grav_y_ball = 11;

// BAll Speed
var speed_x;
var speed_y;

// Character speed_y
var userSpeed_y = {};

// Character is shooting
var userShoot = {};
var shootTime = {}; // shooting maintain time

var debugTxt = null;

var numUser = 0;

var lastTime =0;
var time = 0;

var touchLimite = {};
var win_Score = 0;
var isCheckCollision = 1; // 1: do, 0: undo
var delayTime = 0;

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
    ballMode = 'ready';
    //debugTxt.setText("asdfsfasdfasdfasfasdf" + touchLimite[0] + "  1: "+touchLimite[1]);
    //$('#display_progress .progress-bar' ).css('width','90%');
    $.gameStart();

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
        radius: 25,
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
        fill:'grey',
        stroke: 'black',
        strokeWidth: 2
    });

    // Ground
    ground = new Kinetic.Rect({
        x:0,
        y:595,
        width:800,
        height:5,
        fill:'blue',
        stroke : 'black',
        strokeWidth: 2
    });

    // ScoreBoard
    scoreBoard_1 = new Kinetic.Text({
        x:100,
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

    //CountDownField
    countDownField = new Kinetic.Text({
        x : 380,
        y : 200,
        fontSize : 70,
        fontFamily : 'Calibri',
        fontStyle : 'bold',
        text : '',
        fill : 'red'
    }) ;

    // Debug Text
    debugTxt = new Kinetic.Text({
        x: 0,
        y: 15,
        text: 'Debug',
        fontSize: 10,
        fontFamily: 'Calibri',
        fill: 'red'
    });

    bgimage = new Image();

    var imageObj = new Image();
    imageObj.onload = function() {
        var background = new Kinetic.Image({
            x: 0,
            y: 0,
            image: imageObj,
            width: 800,
            height: 600
        });

        // add the shape to the layer
        layer.add(background);
        layer.add(debugTxt);


        // add the shape to the layer
        layer.add(ball);
        layer.add(fence);
        layer.add(scoreBoard_1);
        layer.add(scoreBoard_2);
        layer.add(countDownField);
        layer.add(ground);


        stage.add(layer);
    };
    imageObj.src = bg_pic;


}
$.settingScore = function(i){      ////게임전 반드시 수행. 안하면 default value;
    win_Score = i;
}

$.initVar = function() {
    speed_x = 0;
    speed_y = 0;
    touchLimite[0] =0;
    touchLimite[1] =0;
    userSpeed_y[0] = 0;
    userSpeed_y[1] = 0;
    userShoot[0] = 0;
    userShoot[1] = 0;

    doubleNet = 0;
    //win_Score = 11; //default value;
}

$.setUserImage = function(seq, pic_url) {

}


$.setUserXY = function(user_type, id,x,y) {

    if(user_type == 0){
        if(kimages[user_type].getX() + x >=0 && kimages[user_type].getX() + x < 315){
            kimages[user_type].setX(kimages[user_type].getX()+x);
        }

        //kimages[user_type].setY(y);

        layer.add(kimages[user_type]);

        stage.add(layer);
    }else if(user_type == 1){
        if(kimages[user_type].getX() + x >=405 && kimages[user_type].getX() + x < 720){
            kimages[user_type].setX(kimages[user_type].getX()+x);
        }

        //kimages[user_type].setY(y);

        layer.add(kimages[user_type]);

        stage.add(layer);
    }

}

var images = {};

$.addOwner = function(id,name,pic_url) {
    numUser++;
    var user_type = 0;
    var user_x,user_y=500;

    userInfo[user_type] = {};

    //110 500 610 500
    if(user_type == 0){
        user_x = 100;

    }else if(user_type == 1){
        user_x = 610;
    }
    userInfo[user_type].id = id;
    userInfo[user_type].name = name;
    images[user_type] = new Image();

    images[user_type].onload = function() {
        //alert(images[numUser].src);
        kimages[user_type] = new Kinetic.Image({
            image: images[user_type],
            x: user_x,
            y: user_y,
            width:80,
            height:80,
            draggable: true         //나중에 여기 끄기
        });

        kimages[user_type].setOffset(40, 40);

        //alert(data[numUser].id+"/"+data[numUser].x+"/"+data[numUser].y);
        layer.add(kimages[user_type]);

        stage.add(layer);
    };

    images[user_type].src = pic_url;
}

$.addUser = function(user_type,user) {
    numUser++;
    //var user_type = user.user_type;
    var user_x,user_y=500;

    //110 500 610 500
    if(userInfo[user_type] == null) {
        if(user_type == 0){
            user_x = 110;

        }else if(user_type == 1){
            user_x = 610;
        }
        userInfo[user_type] = user;
        images[user_type] = new Image();

        images[user_type].onload = function() {
            //alert(images[numUser].src);
            kimages[user_type] = new Kinetic.Image({
                image: images[user_type],
                x: user_x,
                y: user_y,
                width:80,
                height:80,
                draggable: true       //나중에 여기 끄기
            });

            kimages[user_type].setOffset(40, 40);

            //alert(data[numUser].id+"/"+data[numUser].x+"/"+data[numUser].y);
            layer.add(kimages[user_type]);

            stage.add(layer);
        };

        images[user_type].src = user.pic_url;
    }



}


$.boundaryCheck = function(){

    if(ball.getX() <=25){
        doubleNet = 0;
        if(speed_x < 0)
            speed_x = speed_x * -1;

    }
    if(ball.getX() >= 775){
        doubleNet = 0;
        if(speed_x > 0)
            speed_x = speed_x * -1;

    }
    if(ball.getY() <= 25){
        doubleNet = 0;
        if(speed_y < 0)
            speed_y = speed_y * -1;

    }
}
$.writeWin = function(i){
    //디비쓰기
}
$.writeLose = function(i){
    //디비쓰기
}
/*
 $.calculateScore_threeTouch = function(i){

 if(i == 0){
 ball.setX(630);
 ball.setY(70);
 score_B++;
 scoreBoard_2.setText(score_B);
 }else{
 ball.setX(150);
 ball.setY(70);
 score_A++;
 scoreBoard_1.setText(score_A);
 }
 if(score_A >= win_Score){
 $.writeWin(0);
 $.writeLose(1);
 score_A = 0;
 score_B = 0;
 scoreBoard_1.setText(score_A);
 scoreBoard_2.setText(score_B);

 ballMode ='ready';

 }
 else if(score_B >= win_Score){
 $.writeWin(1);
 $.writeLose(0);
 score_A = 0;
 score_B = 0;
 scoreBoard_1.setText(score_A);
 scoreBoard_2.setText(score_B);

 ballMode ='ready';

 }else{
 kimages[0].setX(100);             //서버에서 날아오는 이전의 캐릭터 위치로 다시 돌아감!!
 kimages[0].setY(500);

 kimages[1].setX(610);
 speed_x = 0;

 kimages[1].setY(500);
 speed_y = 0;

 ballMode = "reset";
 }


 }
 */
$.calculateScore = function(){

    if(ball.getY() > gHeight){

        if(ball.getX() < 400){
            ball.setX(630);
            ball.setY(70);
            score_B++;
            scoreBoard_2.setText(score_B);
        }else{
            ball.setX(150);
            ball.setY(70);
            score_A++;
            scoreBoard_1.setText(score_A);
        }

        if(score_A >= win_Score){
            $.writeWin(0);
            $.writeLose(1);
            score_A = 0;
            score_B = 0;
            scoreBoard_1.setText(score_A);
            scoreBoard_2.setText(score_B);
            $.setOwnerButton();
            alert(userInfo[0].name+" 님이 승리하셨습니다!");

            $.writeResult(0,userInfo);
            ballMode ='ready';
        }
        else if(score_B >= win_Score){
            $.writeWin(1);
            $.writeLose(0);
            score_A = 0;
            score_B = 0;
            scoreBoard_1.setText(score_A);
            scoreBoard_2.setText(score_B);
            $.setOwnerButton();
            alert(userInfo[1].name+" 님이 승리하셨습니다!");
            $.writeResult(1,userInfo);
            ballMode ='ready';

        }else{
            kimages[0].setX(100);             //서버에서 날아오는 이전의 캐릭터 위치로 다시 돌아감!!
            kimages[0].setY(500);

            kimages[1].setX(610);
            speed_x = 0;

            kimages[1].setY(500);
            speed_y = 0;

            ballMode = "reset";
        }

    }


}

$.jumpAction = function(i){
    //debugTxt.setText("저어어어어어엄프!");
    if(userSpeed_y[i] == 0 ){
        kimages[i].setY(499);
        userSpeed_y [i] = -80;
    }
}

$.startShootingAction = function(i){
    //debugTxt.setText("슛 시작!");
    userShoot[i] = 1;
}

$.endShootingAction = function(i){
    //debugTxt.setText("슛 끝!");
    userShoot[i] = 0;
}
$.shooting = function(i){
    if(i == 0 ){
        if(userShoot[0] == 0){
            shootTime[0] = (new Date()).getTime();
            user1_Shoot.start();
        }
    }else{

        if(userShoot[1] == 0){
            shootTime[1] = (new Date()).getTime();
            user2_Shoot.start();
        }
    }
}

$.initAnim = function(){
    var firstTime = 0;

    user1_Shoot = new Kinetic.Animation(function(frame){


        $.startShootingAction(0);

        if( (new Date()).getTime() -shootTime[0] >= 600){
            $.endShootingAction(0);
            this.stop();
        }

    });
    user2_Shoot = new Kinetic.Animation(function(frame){
        $.startShootingAction(1);

        if( (new Date()).getTime() - shootTime[1] >= 600 ){
            $.endShootingAction(1);
            this.stop();
        }

    });
    delay = new Kinetic.Animation(function (){
        isCheckCollision = 0
        //debugTxt.setText("기둘!");
        if( (new Date()).getTime() - delayTime >= 50 ){
            //debugTxt.setText("다시 ㄱㄱ!");
            isCheckCollision = 1;
            this.stop();
        }
    });


    anim_countdown = new Kinetic.Animation(function(frame){
        lastTime =(new Date()).getTime();

        if(lastTime - time < 1000){
            countDownField.setText("3");

        }else if(lastTime - time < 2000){

            countDownField.setText("2");

        }else if(lastTime - time < 3000){

            countDownField.setText("1");

        }else{
            countDownField.setText("GO!");

            if(lastTime - time > 3500) {
                countDownField.setText("");
                ballMode = 'move';
                time = 0;
                this.stop();
            }
        }
    });

    anim = new Kinetic.Animation(function(frame){

        firstTime = 0;

        if((frame.time / 35) - firstTime >= 1){
            //console.log("check");
            firstTime = frame.time / 35;

        }
        if (ballMode == 'move'){
            //$.callAjax();
            var tmod = (frame.timeDiff) * 0.005;

            // 속도 만큼 공을 움직인다.
            ball.setX(ball.getX() + (speed_x * tmod));
            ball.setY(ball.getY() + (speed_y * tmod));
            // 속도가 중력가속도의 영향을 받는다.
            speed_y += grav_y_ball * tmod;
            for(var i = 0 ; i< numUser; i++ ){
                if( kimages[i].getY() < 500){
                    if(kimages[i].getY() + (userSpeed_y[i] * tmod) > 500)
                        kimages[i].setY(500);
                    else
                        kimages[i].setY(kimages[i].getY() + (userSpeed_y[i] * tmod));

                    userSpeed_y[i] += grav_y * tmod;
                }else{
                    userSpeed_y[i] = 0;
                }
            }
            $.boundaryCheck();
            $.calculateScore();

            if(isCheckCollision == 1){
                $.isBallTouched(ball.getX(),ball.getY());// 사람체크
                $.isCollisionToNet(ball.getX(),ball.getY()); // 울타리체크
            }

        }else if (ballMode == 'pause'){
            //countDownField.setText("중지!");
        }
        else if(ballMode == 'resume'){
            $.countDown();
        }
        else if( ballMode =='ready' ){
            if(kimages[0] != null){
                kimages[0].setX(100);
                kimages[0].setY(500);
            }
            if(kimages[1] != null){
                kimages[1].setX(610);
                kimages[1].setY(500);
            }

            ball.setX(150);
            ball.setY(70);

            speed_x = 0;
            speed_y = 0;
        }else if(ballMode =='reset'){
            $.initVar();
            $.countDown();
        }
        else{

        }             //ballMode == stop 이면
        /*else{    ballMode = 'ready';
         this.stop();
         }
         */
    },layer);
}

$.isBallTouched = function(x1,y1){
    //    debugTxt.setText("0 :" + touchLimite[0] + "  1: "+touchLimite[1]);

    for(i = 0 ; i < numUser ; i++){
        var character_x =  kimages[i].getX() +40;
        var character_y =  kimages[i].getY() +40;

        var distance = (x1-character_x)*(x1-character_x) + (y1 - character_y) * (y1 - character_y);

        if(distance < 4225){

            /*if(++touchLimite[i] >= 4){          //error가 있음. 하스브엉
             $.calculateScore_threeTouch(i);
             return;
             } */


            if(i == 0 ){
                touchLimite[1] = 0;
            }
            else
                touchLimite[0] = 0;
            doubleNet = 0;
            $.collisionDelay();

            if(userShoot[i] == 1){
                //debugTxt.setText("슈우우우우우웃!");
                var ii = 220;
                speed_y =  10000 / Math.abs( x1 -  fence.getX() );
                debugTxt.setText("네트 위 충돌! :" +  speed_y);
                if(x1 < 400)
                    speed_x = ii;
                else
                    speed_x = -ii;
            }
            else{
                //speed_y = -70;
                speed_y = (y1 - (kimages[i].getY()+40)) - 20;
                speed_x = (x1 - (kimages[i].getX()+40)) * 1.5 ;
            }

        }
        else{ }
    }

}

$.isCollisionToNet = function(x1,y1){

    var x2 = fence.getX()  ;
    var x3 = fence.getX() +10;

    var y2 = fence.getY() ;
    //var y3 = fence.getY() + 200;

    var distance = x1 - (x2+5) ;

    if( (( y2 <=  y1+20) && ( y1+20 <= y2+5 )) && ((x2 <= x1)&&(x1 <= x3)) && doubleNet != 1 ){ // 네트 위 충돌
        var tempX = -1 * speed_x;

        if( tempX < 0 ) tempX -= -1;

        if(speed_x < speed_y){
            //debugTxt.setText("네트 위 충돌!");
            speed_y = speed_y * -1;     ////////////////////////////////////////////////// 수정
            doubleNet = 1;
            $.collisionDelay();
        }

    }else if((y1 > y2 -25) && ( -30 <= distance  && distance <= 30 ) && doubleNet != 1 ){ //
        speed_x = speed_x * -0.8;
        doubleNet = 1;
        $.collisionDelay();

    }

}
$.collisionDelay = function(){
    delayTime =(new Date()).getTime();

    delay.start();
}
$.countDown = function(){
    if(time == 0 )
        time =(new Date()).getTime();

    anim_countdown.start();
}
$.gameStart = function() {
    anim.start();
}

$.Init();