package org.horiga.study.armeria.http.service

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.common.Slf4jNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import org.assertj.core.api.Assertions
import org.horiga.study.armeria.http.configuration.MyApplicationProperties
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import reactor.test.StepVerifier

class BookServiceTest {

    companion object {
        val log = LoggerFactory.getLogger(BookService::class.java)!!
        val objectMapper = jacksonObjectMapper()
            .registerModule(JavaTimeModule())
            .configure(SerializationFeature.INDENT_OUTPUT, false)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true)
            .configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    lateinit var mockserver: WireMockServer

    @BeforeEach
    fun setUp() {
        mockserver = WireMockServer(options().dynamicPort().notifier(Slf4jNotifier(true))).apply {
            this.start()
        }
    }

    @Test
    fun success() {
        val s = BookService(
            objectMapper,
            MyApplicationProperties(
                MyApplicationProperties.BookProperties(
                    "http://localhost:${mockserver.port()}"
                )
            )
        )
        // response from 'https://api.openbd.jp/v1/get?isbn=978-4-7808-0204-7'
        val json =
            "[{\"onix\": {\"RecordReference\": \"9784780802047\", \"NotificationType\": \"03\", \"ProductIdentifier\": {\"ProductIDType\": \"15\", \"IDValue\": \"9784780802047\"}, \"DescriptiveDetail\": {\"ProductComposition\": \"00\", \"ProductForm\": \"BA\", \"ProductFormDetail\": \"B108\", \"TitleDetail\": {\"TitleType\": \"01\", \"TitleElement\": {\"TitleElementLevel\": \"01\", \"TitleText\": {\"collationkey\": \"オニギリレシピイチマルイチ\", \"content\": \"おにぎりレシピ101\"}, \"Subtitle\": {\"collationkey\": \"エヴリディオニギリイチマルイチヘルシーイージージャパニーズライスボールレシピズ\", \"content\": \"EVERYDAY ONIGIRI 101 Healthy, Easy Japanese Riceball Recipes\"}}}, \"Contributor\": [{\"SequenceNumber\": \"1\", \"ContributorRole\": [\"A01\"], \"PersonName\": {\"collationkey\": \"ヤマダ レイコ\", \"content\": \"山田　玲子\"}, \"BiographicalNote\": \"クッキングアドバイザー。\\n東京・浜田山の自宅にて料理教室「Salon de R」を主宰。「マダムなおうちごはん」から「おもてなしのコーディネイト」を楽しく大胆に調理する方法を伝授し、笑いあふれる料理教室として人気沸騰中。\\n国内での出張料理教室のみならず、食は一番身近な外交と、NYやヒューストン、シンガポール、韓国など海外でも定期的に料理教室を開催。facebookで「おにぎり外交倶楽部」を立ち上げ、おにぎりの輪を世界に広げている。\\n\\nReiko Yamada (Cooking adviser)\\nReiko hosts “Salon de R,” a cooking workshop at her private home in Tokyo. Her 30 years of experience as a food coordinator at a children’s international summer camp inspired the idea of food as the most basic form of diplomacy. She now hosts workshops throughout Japan and internationally in Singapore, Korea, New York City, and Houston. In addition, Reiko develops recipes for companies, and contributes to magazines and radio. Most recently, she launched a Facebook page called “Onigiri Diplomacy Club” to spread the onigiri circle around the world.\"}, {\"SequenceNumber\": \"2\", \"ContributorRole\": [\"A01\"], \"PersonName\": {\"collationkey\": \"ミズノ ナオ\", \"content\": \"水野　菜生\"}}], \"Language\": [{\"LanguageRole\": \"01\", \"LanguageCode\": \"jpn\", \"CountryCode\": \"JP\"}], \"Extent\": [{\"ExtentType\": \"11\", \"ExtentValue\": \"126\", \"ExtentUnit\": \"03\"}], \"Subject\": [{\"SubjectSchemeIdentifier\": \"78\", \"SubjectCode\": \"0077\"}, {\"SubjectSchemeIdentifier\": \"79\", \"SubjectCode\": \"07\"}]}, \"CollateralDetail\": {\"TextContent\": [{\"TextType\": \"02\", \"ContentAudience\": \"00\", \"Text\": \"海外でも人気の日本のソウルフード、おにぎり。クッキングアドバイザー・山田玲子が考えた101のレシピを英訳付きでご紹介します。\"}, {\"TextType\": \"03\", \"ContentAudience\": \"00\", \"Text\": \"101人いれば、101通り、好みのおにぎりがあります。\\nマイおにぎりを作ってもらうためのヒントになればと、クッキングアドバイザー・山田玲子が101のおにぎりレシピを考えました。全文英訳付き。\\n日本のソウルフード、easy、simple、healthyなおにぎりは海外でも人気です。\\n外国の方へのプレゼントなど、小さな外交がこの本から始まります。　　\\n\\nOnigiri—a healthy fast food—is the soul food of the Japanese. Although it may not be as widely recognized as sushi, onigiri is synonymous with the phrase “taste of home,” and is a staple of Japanese comfort food. Its simplicity—just combining rice and toppings—offers endless possibilities without borders. The portable onigiri can be served in all kinds of situations. It’s perfect for bento lunch, as a light snack, or even as party food. \\nReiko Yamada’s 101 simple and easy riceball (onigiri) recipes include mixed, grilled, sushi-style onigiri and more! This cookbook is a perfect introduction to the art of onigiri-making, filled with unique recipes that are bound to inspire your Japanese culinary creativity. Pick up a copy, and you’ll become an onigiri expert in no time!\"}, {\"TextType\": \"04\", \"ContentAudience\": \"00\", \"Text\": \"はじめに　INTRODUCTION ……002\\nこの本の使い方　HOW TO USE THIS COOKBOOK……002\\nご飯の炊き方　HOW TO COOK RICE ……008\\n塩のこと　ALL ABOUT SALT ……010\\nおにぎりのにぎり方　HOW TO MAKE ONIGIRI ……012\\nおにぎりと具材の相性　ONIGIRI COMBINATIONS……014\\nのりの使い方　HOW TO USE  NORI SEAWEED ……016\\n\\n1. 中に入れる FILL……019\\n2. 混ぜる MIX……037\\n3. 炊き込む／焼く TAKIKOMI / GRILL……067\\n4. のせる／つつむ SUSHI-STYLE / WRAP……085\\n\\nおかずレシピ　OKAZU RECIPE……106\\nおにぎりを冷凍保存　HOW TO KEEP LEFTOVER ONIGIRI ……113\\nおにぎりをお弁当に詰める　HOW TO PACK A BENTO ……104\\nINGREDIENT GLOSSARY ……116\\nこの本で使用したもの　PRODUCT LIST ……120\\nプロフィール　PROFILE……121\\nおわりに　CLOSING ……122\"}], \"SupportingResource\": [{\"ResourceContentType\": \"01\", \"ContentAudience\": \"01\", \"ResourceMode\": \"03\", \"ResourceVersion\": [{\"ResourceForm\": \"02\", \"ResourceVersionFeature\": [{\"ResourceVersionFeatureType\": \"01\", \"FeatureValue\": \"D502\"}, {\"ResourceVersionFeatureType\": \"04\", \"FeatureValue\": \"9784780802047.jpg\"}], \"ResourceLink\": \"https://cover.openbd.jp/9784780802047.jpg\"}]}]}, \"PublishingDetail\": {\"Imprint\": {\"ImprintIdentifier\": [{\"ImprintIDType\": \"24\", \"IDValue\": \"3795\"}, {\"ImprintIDType\": \"19\", \"IDValue\": \"7808\"}], \"ImprintName\": \"ポット出版\"}, \"PublishingDate\": [{\"PublishingDateRole\": \"01\", \"Date\": \"20140408\"}, {\"PublishingDateRole\": \"25\", \"Date\": \"20140326\"}, {\"PublishingDateRole\": \"09\", \"Date\": \"20140224\"}]}, \"ProductSupply\": {\"SupplyDetail\": {\"ReturnsConditions\": {\"ReturnsCodeType\": \"04\", \"ReturnsCode\": \"03\"}, \"ProductAvailability\": \"99\", \"Price\": [{\"PriceType\": \"01\", \"PriceAmount\": \"1300\", \"CurrencyCode\": \"JPY\"}]}}}, \"hanmoto\": {\"toji\": \"並製\", \"zaiko\": 11, \"maegakinado\": \"おにぎりは日本人のソウルフード、ヘルシーなファーストフードです。\\n和食が世界で注目される中、おにぎりの知名度はお寿司にはとうてい及びませんが、\\n「おふくろの味」とも言われ、日本の家庭料理の代表格です。\\nご飯とおかずをにぎるだけのシンプルさ。ご飯に合うものならなんでも美味しい。\\n組み合わせる具材は、国境を越え無限に広がります。\\n持ち歩き自由でどこでも食べられ、お弁当に、おやつに、夜食に、パーティーにと\\nTPOに合わせて、どんなシチュエーションにも登場可能。\\nおにぎりは「おむすび」ともいい、手で「にぎる」「むすぶ」行為そのものを表す\\n言葉です。人は心を伝えるときに、手を繋いだり、握手したりします。\\n手でにぎるおにぎりは、にぎる人の「気持ち」「温もり」という最高の調味料が加わり、\\n美味しい「ごちそう」になります。\\n\\nOnigiri—a healthy fast food—is the soul food of the Japanese. Although it may not be as widely recognized as sushi, onigiri is synonymous with the phrase “taste of home,” and is a staple of Japanese comfort food. Its simplicity—just combining rice and toppings—offers endless possibilities without borders. The portable onigiri can be served in all kinds of situations. It’s perfect for bento lunch, as a light snack, or even as party food. Onigiri is also called “Omusubi,” which in Japanese means to bind or tie. When we make onigiri, we “bind” the rice together with our hands. Similarly, we hold or shake the hands of others when we express ourselves. When onigiri is made by hand, the ball of rice is seasoned with the best ingredients—the warmth and love of the person who makes it—and transforms into a delicious meal.\", \"kaisetsu105w\": \"日本のソウルフード、easy、simple、healthyなおにぎりは海外でも人気!　クッキングアドバイザー・山田玲子が101のおにぎりレシピを考えました。全文英訳付き。外国の方へのプレゼントにもおすすめです。\", \"tsuiki\": \"●プロモーション・ビデオ\\n\\n\\n○EVERYDAY ONIGIRI\\n\\n\\n\\n\\n\\n\\nhttps://www.youtube.com/watch?v=tUXQ1in-AW0\\n\\n\", \"genrecodetrc\": 14, \"kankoukeitai\": \"BCD\", \"jyuhan\": [{\"date\": \"2014-05-02\", \"ctime\": \"0000-00-00 00:00:00\", \"suri\": 2}, {\"date\": \"2014-05-28\", \"ctime\": \"0000-00-00 00:00:00\", \"suri\": 3}, {\"date\": \"2014-07-28\", \"ctime\": \"0000-00-00 00:00:00\", \"suri\": 4}, {\"date\": \"2014-12-12\", \"ctime\": \"2014-12-19 14:28:40\", \"suri\": 5, \"comment\": \"日本語・英語の2ヵ国語で、\\n見て楽しい、食べておいしいおにぎり101種類を紹介。\\n海外書店でも売れ行き好調です。\"}, {\"date\": \"2015-03-20\", \"ctime\": \"2015-03-18 19:12:27\", \"suri\": 6, \"comment\": \"国内のみならず海外でも引き続き好調です。\\n1年で1万部の刷部数となりました。\"}, {\"date\": \"2015-08-03\", \"ctime\": \"2015-08-03 16:35:14\", \"suri\": 7}, {\"date\": \"2016-02-03\", \"ctime\": \"2016-02-18 21:38:27\", \"suri\": 8}, {\"date\": \"2016-06-24\", \"ctime\": \"0000-00-00 00:00:00\", \"suri\": 9}, {\"date\": \"2017-05-24\", \"ctime\": \"2017-05-15 10:44:42\", \"suri\": 10, \"comment\": \"ついに10刷到達！\\nおにぎりは日本のソウルフード。\"}], \"hastameshiyomi\": true, \"author\": [{\"listseq\": 1, \"dokujikubun\": \"著\"}, {\"listseq\": 2, \"dokujikubun\": \"英訳\"}], \"datemodified\": \"2017-08-16 19:03:10\", \"datecreated\": \"2014-02-25 11:29:44\", \"reviews\": [{\"post_user\": \"genkina\", \"reviewer\": \"\", \"source_id\": 29, \"kubun_id\": 1, \"source\": \"毎日新聞\", \"choyukan\": \"\", \"han\": \"\", \"link\": \"\", \"appearance\": \"2014-04-13\", \"gou\": \"\"}], \"hanmotoinfo\": {\"name\": \"ポット出版\", \"yomi\": \"ポットシュッパン\", \"url\": \"http://www.pot.co.jp/\", \"twitter\": \"https://twitter.com/potpub\", \"facebook\": \"\"}, \"dateshuppan\": \"2014-04\"}, \"summary\": {\"isbn\": \"9784780802047\", \"title\": \"おにぎりレシピ101\", \"volume\": \"\", \"series\": \"\", \"publisher\": \"ポット出版\", \"pubdate\": \"20140408\", \"cover\": \"https://cover.openbd.jp/9784780802047.jpg\", \"author\": \"山田玲子／著 水野菜生／英訳\"}}]"

        mockserver.stubFor(
            get(urlPathEqualTo("/v1/get"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(json)
                )
        )

        val isbn = "978-4-7808-0204-7"
        StepVerifier.create(s.findByIsbn(isbn))
            .consumeNextWith {
                log.info(">> $it")
                Assertions.assertThat(it.summary.isbn).isEqualTo(isbn.replace("-", ""))
                Assertions.assertThat(it.summary.cover).isEqualTo("https://cover.openbd.jp/9784780802047.jpg")
                Assertions.assertThat(it.summary.title).isNotBlank()
                Assertions.assertThat(it.summary.pubdate).isNotBlank()
                Assertions.assertThat(it.summary.title).isNotBlank()
            }.verifyComplete()
    }
}
