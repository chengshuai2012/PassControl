package com.link.cloud.network.bean;


import java.util.List;

import io.realm.RealmList;

/**
 * Created by 49488 on 2018/12/18.
 */

public class AllUserFaceBean {
    /**
     * total : 3
     * data : [{"id":17,"merchantId":1,"uid":"i2wk31hg","uuid":"406afce8a19b453bb77f6312a14b5a8b","phone":"13726261348","sex":1,"headImg":"","fingerprint":null,"userType":1,"status":1,"createTime":"2018-10-20 18:21:03","nickname":""},{"id":18,"merchantId":1,"uid":"mhlqirbc","uuid":"b7e97bf9cb544003828d54265b774b2e","phone":"13726266729","sex":1,"headImg":"","fingerprint":null,"userType":1,"status":1,"createTime":"2018-10-20 18:40:29","nickname":""},{"id":19,"merchantId":1,"uid":"wqe9popz","uuid":"c2ef5a5e995e48b08b7650f0648b52b2","phone":"18574107629","sex":1,"headImg":"","fingerprint":"434344336674566424523223578374754432423727745485442364552435685553240524853558277523355455333558474343646542447597433247845233447642333494733446733254348454555453345435552475577432424776434466734222488741434875321228855235286432234588333359345334466643356643233453443759751123443133269C65153163236A525851247653223796443212695212269934321214964114439A52023366301245BA434222366322369964531243775433847565221435A4434556532215588223277852123547524355883324344424388676753135553542478482222428855146557211323B743163475513553A66153557431346545425692632134863443349643233455335445A7339537532394364232942735139535363282524335745529412427374455534872354571121379A231135C411222698222126C3213226963323354632354A773333356542334A6552444771423268716424284332232C9334353467433636656223354834363588534256634335669433434424337577464714375312553A7543331864135518953333266333464694444524442386485552246643233767744423376223274A822423374133353B9423375333324A75543326366222475A542323289222323A743312363232466A8344143492112578A3211333B4211453A721223686323344A732243566433348863423467433354966342336923334488631333463464245647843554333535A35473436733136668734222984321438772422576314533A9115133394333448933523477321354B8312123B621113888322146763121469942211278320122BB521111383201229D7211144562113668B123254A113355955234344A233354695223255A322357684113421783244348A33331159423324B943443354524335B753355553434248845547584222554A23233547443244795531222364332278B722124794222146A5442337741415597345145783223795533313696200248953121195432123C84431124544213479A72212282220214BB711133774221348B621223583212356C552223674322237C54311594311213BB42121646322624B843432383341434B85322228773123488734243444515849645324464321376B463124576533256945331237955313557553000A7410121BA322113A5321222C932314477322253987130127853113299733124384222264C742235534432466984212764122329B632325354122245C922213253311124DA543220554322229C563132555542324B6654235544363389544732254427434B433622448555135465633282463636A433433454432785855324345645364459342313595413335B45111444411134CD331226644224348B6141245533314288A541133355323468895113575111256C841122673322528B74202375001154F821124345233222CC04425222253696523957643233677442356774322476744125761103373962356839311346693111467822101568432136B43346665344456664342016406656A364555655455647454333274325460D25464A3CC6164E361A31D1DEF10FE00000064676461E4E1E9A31F7F73B8FE000000C4EBC423E4E5E5270859A3008E000000C391C20DE465E537C0521305A7000000C711CF11D0E5E3C19119F0FFA4000000CF49DD31D1C5E3C1313BB3C7E50000006B087B2879C47989F477BFF3A90000006B02EB2271C83988F414A5AAC90000007A626C41381971197F4FFD9DC40000007AC3F3217C396D90CF6FBEF1FC000000FD83EF83FD99FD99EFED3CD1C9000000BD91BD31F991F981F73D38DFCB000000E488E188F900FB092131D0FFCE000000E441E4C0F828F1013450409FEE00000037A647A666A363A30BDDF17FFE00000064A764E0E6A663A303F5701C9E000000C841DC61E4E4C5A6236373EC8C000000C9D1C8E0E4E4C4A66A6333F88C000000C207C6D1D0ECC1AB8D0D6593AC000000535BDD69D1EDD3CB1D1A7380AC0000006513798FB1CD30C9BCF6D34728000000252B2F2D31183189BDA5836758000000394C38C4391971895B4F77C7FC00000039C9F1ED3938399B4747466FFE0000003D87AD21BD88BD0BFF3F2453C80000007C156CB1B984BD0C7D3D20DE88000000BCA1B983F800F92979FF98448C000000F041B0E3F828F9297C4C489C8E0000003316B32F6E263B268FDFEC63FE0000006A860FC96E666BA603C7E823AE00000048E32CE3E466C5B64363F916AC000000CAF16E61E4E4C4AC6A6BE15CAC00000094EBCCF7D46ED1AE 10-20 10:50:05.817 4448-4471/com.link.cloud D/OkHttp: C96935FBAC000000467B466394EED3AAE9FDD5022C0000002669C738308D32C97CB5904E6C0000002239B359309D3289BD21027C7C000000311639B6784E34CF8291F9EFFC000000726F71AF188919A311049AFFCC0000003C472CF7B98CBD277F5C7CB8E90000001C853C95B924BC247DDF6C90090000003CE73E67B832BA29484FBC64880000003CE332673810BB094C4F7CB18800000017360B462E362A77FFDEC4060A0000001EB60B672E662B36F9DF44238A0000003C9B243744EE4DBE0B0FDF40A80000002CB32CB544EE4C9C6B4BCDD0A000000085AB85E744AE409A494D5D00E60000008DB3C5F554AE42AAE8E854006600000000BB92F9568F64EBB83862C86E000000809BA3F1368D30EB0C1CE67D7E000000966386F833CD12C771B0B0CE4D000000526EC4EE138F30E31414D4EFEB0000009C8D4E4399279CA5000884F2610000001C855E4799A59CA51191C4B2410000003C65342FB8B33633407ABEE0000000002671066D3093323B5012DEFE8C0000001BB45AB52E7C2A6BF6E625990E000000139C1BAD267C2A69FAFE449D0E00000015799BB84A6C4858E984E66DA000000005390B39486C48D8A185EC33A1000000E7EDC5B34CAEC0D9C8C82FC266000000C3B8C1F24C8E60D9C8C4CCD326000000CAA75AE74E8B4CDA0C4843AB2600000088EB82E55E8B2ADB0C0CC0BB6200000098DB08CB470B471F6A7D459BC100000098330AC39B27933338185482E30000004C33CE6799A78F91160C4CF2E10000004E6B4A6399B18E93151E4E3A61000000473363B2B1B336F3B0FA7ECB05000000473263F131B3373374707E1F8D00000019CC19E44CFC097CD3E36D3C9E0000007BCC33EC0EDC096CE3E2616DCE000000D998D8A8425C49C8B3970048EF000000C91CC870427CC8C8B3944E09EF000000676C871C419EC848F090AD4E6F00000067CCA78C69DE685CF0D08B7F2F000000C8A5887F4F8A460A080803C002000000CC6D88D74F83438B0C080AB22200000019D399C3870337133A48C925B2000000393326971B0723117A1C1D32E60000001B0B1E6D93338713535B199EE00000004E8B1E0AA3B18393135391DEA00000005B327372B3F137F1B31352060000000072316AB133F113733737108F0C000000198E316BCDFC09BECBEB6FFF9E0000005B967332445C49BEC7C72FFFDE000000D80FD836665C4DCAFFBF3EFDEF000000C82CD82CE03CC8CAFFFF7EFDEF000000E66CE72C619CCC4CF878B1E37B000000C74CD73C659C484CB83883E739000000C1B4C996478A478E0000C00002000000CC8D5CC7C783478B0222C3C00300000018B9E8D917072325332106DFFE000000F161F8C92319A711571645D1FF0000001F09D62197332793030735E7E90000005E1A7E18B33233938787A7FFF80000005E5876D0B3F053D10113609C000000007231D6F1BAF17991000160FF000000001C5F1C5F1C5E1C5E1C5E1B5E1B5D1B5D1B5D1B5D1B5D1B5D1A5D1A5D1A5D1A5E1A5D1A5D1A5E1A5E1A5E1A5E195E195E195E195E195F195F195F185F1860186017601760176017601760176017601761176117611762166216621762166216621762176216621661166117621762176217621762176218621862186218621862186219621962196219611861373C6231322861207A9BABAAA9A7A3A2A19F9EA09C9688838177B3E4FDFCFBF9F6F4F3F1F1F2EFE9DBD5D4C4B4E6FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEEB4E6FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEEB4E6FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEEB2E1F9F8FBFCFEFFFFFFFFFFFFFFFFFFF0E77898A6A6A9ACB0B7BBC0C4C9CBCCCCCDBF9B3E4E545456595D65696D7176797A7A7A725A7F92AEACAAAAA9A8A6A5A29F9482A1B9DCDBD9D8D7D7D5D3D0CDC0AAB9D5FFFFFFFFFFFFFEFDFAF9EDD1B9D5FFFFFFFFFFFFFFFFFFFFF1D6A0B8DDDDE0E2E7E9ECEDEFF0CDB47E91AEAFB2B4B8BABEBFC1C1A5905C6A808184858A8C8F9092937D6DEAE7F6052AD115FAF2DFEC0FFD01090D00FEEB0BD50E09F7F709F606FA05E00611EFEC0B1C1703FAEF0A06F30B05F6FF030402FEFFEFFC00FD08F8FAEA3201E2E03C101BE2040417FCF206FF0C01FAFDF2E9F80CF911FC0002F200EFFDF5FD1A130208FCFF19F60300FBFC0903FD09FB0212FE00F5FEFFFE12C50AE431E012EAF10509000F03130C150C0300D81803FAF80AF1FFF3F9EEF40202F2041B0AFD04EF06030A0808FB0606FFFFF8FCF9FBFF0304F6FCF42B171DC60915010602000304F91FFE0A04F418ECE3F4EDF50D0F0800F6F3E813EE02FA11FF00FF190DF5F1EB0501010212080EFF0708FDF4040904A4F0","userType":1,"status":1,"createTime":"2018-10-20 18:48:37","nickname":""}]
     */

    private int total;
    private RealmList<UserFace> data;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<UserFace> getData() {
        return data;
    }

    public void setData(RealmList<UserFace> data) {
        this.data = data;
    }
}
