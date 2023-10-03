package wiki.IceCream.yuq.demo.Keyboard.router.functions.other;

import com.icecreamqaq.yuq.event.GroupMessageEvent;
import wiki.IceCream.yuq.demo.Keyboard.router.functions.Function;

public class ChoiceAssistant extends Function {

    public ChoiceAssistant(){
        super("选择辅助",
                "choice-assistant",
                "命令：“小助手今天（做）什么\n" +
                        "例：小助手今天吃什么”",
                1);
    }

    @Override
    public void action(long group, long qq, String strMessageText, GroupMessageEvent event) {
        String verb = strMessageText.substring(5).substring(0, strMessageText.length()-7);
        String strFoods = "虾仁缤纷疙瘩汤|粥底火锅|荠菜瘦肉粥|养生黑米粥|竹荪马蹄鸭汤|白果猪肚汤|海鲜豆腐排骨汤|核桃肉煲牛腱汤|羊肉萝卜汤" +
                "|新派羊肉汤|苹果草鱼汤|豆浆鲈鱼汤|蘑菇鱼丸汤|奶油鸡茸蘑菇汤|酸辣菌菇汤|奶香金瓜蘑菇汤|奶油南瓜汤|润肺止咳汤|土豆冷汤|" +
                "紫菜蛋花汤|法式洋葱汤|玉米土豆蘑菇素汤|莼菜汤|香梨萝卜汤|养颜藜麦羹|碎菜鸡蛋羹|秘制安神羹|养生南瓜豆腐羹|雪梨核桃盅|" +
                "醪糟南瓜盅|蟹柳苦瓜盅|薏米养生粥|栗子羹|酒酿小圆子|牛肉粉汤|腌笃鲜｜千层饼|香煎藕饼|蔬菜鸡蛋饼|金汁焗牛肉饼| 脆皮茭瓜饼" +
                "| 香暄媳妇饼 | 牛肉豆腐饼 | 农家粗粮饼 | 山芹鲜虾饼 | 椰香土豆饼 | 菠菜鸡蛋饼 |  红薯鸡蛋饼  | 南瓜鸡蛋饼 | 黄金豆沙饼" +
                "| 香酥椒盐饼 | 家常美味馅饼 | 什锦蔬菜饼 | 芝麻胡萝卜饼 | 芹香煎饼 | 翡翠虾仁米饼  | 香煎蛤肉槐花饼|米饭煎饼|脂渣烩饼" +
                "| 葱香土豆蛋饼 | 南瓜饼 | 金钱洋葱饼 | 墨西哥玉米薄饼 | 莲藕虾饼 | 燕麦煎蛋饼 | 双色火烧 |酥皮糖火烧|杂粮煎饼果子 | " +
                "腊汁肉夹馍|京都马蹄饼 | 千层油饼 | 孜然火烧 | 葱油饼|泡椒双色蛋饺 | 翡翠鱼形饺 | 馄饨 | 元宝饺 |大米一品饺 | 草莓酱煎蛋饺" +
                "  | 黄瓜素饺 | 山珍素饺 | 紫米四喜饺| 酸汤水饺 | 大理石饺子| 鲅鱼饺子 |三鲜水饺|三丁包 | 水煎包 | 俄罗斯包子|茭白虾米石榴包" +
                "|南瓜豆沙包| 火腿牛奶花包 | 燕麦油酥餐包|芸豆生煎包 | 灌汤包 | 红薯包 | 大米烧麦 | 鲜虾烧麦| 茭瓜蛤蜊锅贴|香菇锅贴|茶树菇炉包 " +
                "|家常炒面 | 风味鱼面 | 皮胶美容面 | 茄香炸酱面 | 蝴蝶虾面 | 三更回味面 |麻汁凉面 | 茶香意大利面 | 菌菇烩意面 | 扇贝海鲜面 | " +
                "菇香肝酱拌拉面|豆浆面|芥香牛柳乌冬面|重庆小面|清汤宽心面|麻食猫耳面|鸡肉虾仁面|炒面|肉丝黄瓜炒面|菇香干酱拌拉面" +
                "|炒果条|粗粮卡花| 黑米双色馒头|家常窝窝头|年糕炒油菜|非油炸春卷|自制油条";
        String strDrinks = "可口可乐、百事可乐、雪碧、脉动、果缤纷、果粒橙、营养快线、康师傅冰红茶、绿茶、茉莉花茶、娃哈哈AD钙奶、旺仔牛奶、美年达、芬达、" +
                "o泡果奶、蒙牛、伊利、光明、普罗旺斯饮料、最罗曼饮料、天山雪、汇源、鲜橙多、啤儿茶爽、柠檬C、味全、农夫山泉、青岛啤酒、维他命水、雪花啤酒、" +
                "茉莉蜜茶、茉莉清茶、呦呦奶茶、优活果汁、邂逅饮料、凉生果汁、碧溪果汁、海蓝果汁、叶律素、农夫果园、利趣拿铁、蜂蜜柚子、冰糖雪梨、陈皮酸梅、" +
                "香芒椰果、水晶葡萄、啤儿茶爽、旺仔牛奶、雀巢咖啡、晶莹果汁、德彪西露饮料、曼特林咖啡、意大利咖啡、意大利浓咖啡、意大利泡沫咖啡、拿铁咖啡、" +
                "美式咖啡、法式滴滤咖啡、冰法式滴滤、低因咖啡、曼巴咖啡、速溶咖啡、现磨咖啡、冰咖啡、浓缩冰咖啡、冰薄荷咖啡、冰卡布奇诺、冰焦糖卡布奇诺、" +
                "冰香草卡布奇诺、果味卡布奇诺、薰衣草卡布奇诺、香草卡布奇诺、榛子卡布奇诺、冰拿铁咖啡、冰香草咖啡拿铁";
        String strEntertainments ="ns|wii|3ds|2ds|ps5|psv|psp|ps4|ps3|xbox one|xbox 360|xbox series s|xbox series x|分手厨房|马里奥赛车|明日方舟" +
                "|碧蓝航线|Love Live! School Idol Festival!|Love Live! School Idol Festival All Stars|欧洲卡车模拟|美国卡车模拟|微软模拟飞行2020|白色相簿|秋之回忆";
        switch (verb) {
            case "吃":
                String[] strArrFoods = strFoods.replaceAll("\\s", "").split("\\|");
                sendMessage(event, "小助手今天吃"+strArrFoods[(int)(Math.random()*strArrFoods.length)]+"捏");
                return;
            case "喝" :
                String[] strArrDrinks = strDrinks.replaceAll("\\s", "").split("、");
                sendMessage(event, "小助手今天喝"+strArrDrinks[(int)(Math.random()*strArrDrinks.length)]+"捏");
                return;
            default:
                sendMessage(event, "小助手也不知道今天要"+verb+"什么捏");
        }

    }

    @Override
    protected boolean checkMessage(String strMessageText, GroupMessageEvent event) {
        return strMessageText.matches("小助手今天(.)+什么(捏)?");
    }
}
