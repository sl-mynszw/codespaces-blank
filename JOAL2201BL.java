
// パッケージ名定義
package B.apl.batch;

//java標準API
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import B.gc.common.JCMBAlertMailRap;
import B.gc.common.JCMBCheckCommon;
import B.gc.common.JCMBConstants;
import B.gc.common.JCMBSSARenkeiInfoRegist;
import B.gc.common.JCMBSendMailEditItem;
import B.gc.common.JCMBSendMailGetDBInfo;
import B.gc.common.JCMBSendMailOrderBean;
import B.gc.common.JCMBSendMailUtil;
import B.gc.common.JCMBSendMailWeb;
import B.gc.common.JCMBUtilityCommon;
import B.gc.common.JCMDbAccessCommon;
import B.gc.common.JCMExtUtil;
import B.gc.common.JOABConstants;
import B.gc.web.JOABConstant;
import Z.fw.batch.JBFBusinessException;
import Z.fw.batch.JBFMain;
import Z.fw.batch.JBFSystemException;
import Z.fw.common.JCFException;
import Z.fw.web.JOFConnectionStatusHolder;
import Z.fw.web.JOFConstant;
import Z.fw.web.JOFThreadLocalMap;
import Z.gc.common.JCMMysqlConnection;
import Z.gc.common.JCMMysqlDBAccesser;
import Z.util.common.JCMCommonProperties;
import Z.util.common.JCMLog;
import Z.util.common.JCMMessageManager;

/*
 * バージョン情報定義
 */
class CopyrightJBAPB3204 extends JBAPB32ProgramCopyright {
	public CopyrightJBAPB3204(Object thisOjbect) {
		super(thisOjbect, "Ver1.06", // バージョン情報
				"2006/05/12 18:20:00" // 作成日付
		);
	}
}

/**
 * プレ予約オーダー及びセンカンドチャンスオーダー抽選処理時の当落結果 のメールの送信を行う。<br>
 * <ul>
 * <li>
 * プレ予約オーダーおよびセンカンドチャンスオーダー抽選処理時に作成さ れる「当落メール編集元情報を入力し、当落メールの文面作成と送信処理 を行う。
 * </ul>
 * <b>起動パラメータ</b><br>
 * <code>
 * <pre>
 *  P1=起動パラメータ1
 *  P2=無し
 *  P3=無し
 * </pre>
 * </code> <br>
 * <b>使用例</b><br>
 * <code>
 * <pre>
 *  1. 開始前処理(preStart)
 *  2. 開始後処理(postStart)
 *  3. トランループ前処理(preTrnloop)
 *  4-1. ループ制御判定(loopContinueJudge)
 *  4-2. コミット実行判定(doCommitJudge)
 *  4-3. コミット前処理(preCommit)
 *  4-4. コミット後処理(postCommit)
 *  4-5. トランごとの処理(tranRoutine)
 *  5. コミット前処理(preCommit)
 *  6. コミット後処理(postCommit)
 *  7. 正常終了前処理(preNormalEnd)
 *  8. 業務異常終了前処理(preBusinessAbnormalEnd)
 *  9. システム異常終了前処理(preSystemAbnormalEnd)
 *
 * 4-3, 4-4の処理は4-2. コミット実行判定の返却値がtrueの場合のみ処理を行う
 * 4-1. ループ制御判定の返却値がtrueの間は4-1～4-5を繰り返します
 * </pre>
 * </code>
 *
 * <pre>
 * $History:: JBAPB3204.java                                 $
 *
 * *****************  Version 11  *****************
 * User: Zhangxh      Date: 17/08/02   Time: 18:19
 * Updated in $/eplus/javaapp/src/B/apl/batch
 * 障害-23336（【新会員】抽選結果確認メールが携帯用の文面にな
 * っていた）
 *
 * *****************  Version 10  *****************
 * User: Jincj        Date: 12/12/27   Time: 10:07
 * Updated in $/eplus/javaapp/src/B/apl/batch
 * 改善-12719【【現行継続】当落メール送信に関する配信遅延改善
 * 対応】
 *
 * *****************  Version 9  *****************
 * User: Eusr_b025    Date: 06/06/03   Time: 17:21
 * Updated in $/eplus/javaapp/src/B/apl/batch
 *
 * *****************  Version 8  *****************
 * User: 樋口まり子   Date: 06/06/02   Time: 22:54
 * Updated in $/eplus/javaapp/src/B/apl/batch
 * 2006/06/02 m.higuchi
 * メール部品、初期化、クローズを一括処理に変更
 *
 * *****************  Version 7  *****************
 * User: 市川辰亮     Date: 06/05/27   Time: 15:24
 * Updated in $/eplus/javaapp/src/B/apl/batch
 * 2006/05/27 T.Ichikawa 新ログ対応
 *
 * *****************  Version 6  *****************
 * User: 山田繁毅     Date: 06/05/12   Time: 18:17
 * Updated in $/eplus/javaapp/src/B/apl/batch
 * 2006/05/23 S.Yamada ソースチェック指摘対応
 *
 * *****************  Version 5  *****************
 * User: 樋口まり子   Date: 06/04/24   Time: 16:47
 * Updated in $/eplus/javaapp/src/B/apl/batch
 * 2006/04/24 mhiguchi
 * アラートメール処理追加（コンストラクタのパラメータを変数に
 * 変更）
 *
 * *****************  Version 3  *****************
 * User: 樋口まり子   Date: 06/04/01   Time: 23:00
 * Updated in $/eplus/javaapp/src/B/apl/batch
 * 2006/04/01 mhiguchi 当落メール情報処理条件変更
 *
 * *****************  Version 2  *****************
 * User: 樋口まり子   Date: 06/03/30   Time: 23:15
 * Updated in $/eplus/javaapp/src/B/apl/batch
 * 2006/03/30 mhiguchi 当落メール処理追加
 * </pre>
 *
 * @version $Revision:: 1 $
 * @author $Author:: Zhangxh   $
 * @see
 */
public class JBAPB3204 extends JBFMain {

	/*
	 * バッチアプリ標準の変数定義 (ここで定義されている変数は削除しないこと)
	 */

	private String g_tajyuNo; // 多重番号
	private String g_tajyuDo; // 多重度

	// ループ判定用
	private boolean isLoopContinue = true; // ループ判定フラグ

	// ログ出力用
	private JBAPB32LogUtil g_log = null;

	// コネクション用
	private Connection g_Connection = null;

	// 処理カウンタ(2006.6.2 井上)
	private int g_preTousenMailNum = 0;
	private int g_preRakusenMailNum = 0;
	private int g_sakibaraiPreTousenMailNum = 0;
	private int g_sakibaraiPreRakusenMailNum = 0;

	/*
	 * 各バッチアプリ固有の変数定義
	 */

	private String g_nowDateTime = ""; // バッチ起動時日付時刻

	// テンプレート情報を保存するマップ
	private HashMap templateInfoMap;
	// 区分より必須置換文字用HashMap(携帯用)
	private HashMap mapTikanMonziKeitai;
	// 区分より必須置換文字用HashMap(ＰＣ用)
	private HashMap mapTikanMonziPc;
	// スキン変更HashMap
	private HashMap mapSkinInfo;
	// 区分より必須差込文句用HashMap(バッチ系携帯専用)
	private HashMap mapSasikomiMonkuKeitai;
	// 区分より必須差込文句用HashMap
	private HashMap mapSasikomiMonku;

	private String[] strBtTicketHanvaiSubTeisu;

	private JBAPB32TourakuMailInfo tourakuMailInfo;

	// 障害-23336（【新会員】抽選結果確認メールが携帯用の文面になっていた）ADD START
	private ArrayList mobileDomain = new ArrayList(); //携帯ドメイン
	// 障害-23336（【新会員】抽選結果確認メールが携帯用の文面になっていた）ADD END

	//改善-30866（リアルチケット販売と連動して配信用のオーダーを作成する）ADD START
	private HashMap<String, String> ssaRenkeiUketsukeMap;
	private JCMBSSARenkeiInfoRegist jcmbssaRenkeiInfoRegist;
	//改善-30866（リアルチケット販売と連動して配信用のオーダーを作成する）ADD END

	/**
	 * コンストラクタ
	 *
	 * @param arg0
	 */
	public JBAPB3204(String[] arg0) {
		super(arg0);
	}

	/*
	 * バッチ制御メソッドの記述部(非公開)
	 */

	// 1. 開始前処理(preStart)
	/**
	 * 開始前処理を行います。<br>
	 *
	 * @see Z.fw.batch.JBFMain#preStart()
	 */
	protected void preStart() throws JBFSystemException, JBFBusinessException {

		// プログラムバージョン情報の出力
		new CopyrightJBAPB3204(this);

	}

	// 2. 開始後処理(postStart)
	/**
	 * 開始後処理を行います。<br>
	 *
	 * @see Z.fw.batch.JBFMain#postStart()
	 */
	protected void postStart() throws JBFSystemException, JBFBusinessException {

		// ログ出力オブジェクトの生成
		this.g_log = new JBAPB32LogUtil(msg.getJobName());

		this.g_log.putLog("【postStart】処理開始");

		// コネクションの取得
		this.g_Connection = msg.getConnectionAtB2C();

		JOFThreadLocalMap.set(JOFConstant.Z_DB_CONNECTION_FOR_PARTS, this.g_Connection);
		JOFThreadLocalMap.set(JOFConstant.Z_DB_CONNECTION, this.g_Connection);
		// 部品用コネクション　初期化処終了ステータスセット
		JOFConnectionStatusHolder.setConnectionStatus(JOFConnectionStatusHolder.AFTER_INITIALIATION);

		this.isLoopContinue = true;

		// 起動パラメータ1(P1:多重番号)のチェック　0～9であること
		this.g_tajyuNo = this.getBatchParam("P1");
		this.g_log.putTrace(" 多重番号 [" + this.g_tajyuNo + "]");
		if (!checkNumber(this.g_tajyuNo, 0, 9)) {
			throw new JBFBusinessException("多重番号[P1]=" + this.g_tajyuNo + "です。");

		}

		// プロパティ(多重度)のチェック　1～10であること
		this.g_tajyuDo = JCMCommonProperties.getProperty("TORAKU_MAIL_TAJYUDO");
		this.g_log.putTrace(" 多重度 [" + this.g_tajyuDo + "]");
		if (!checkNumber(this.g_tajyuDo, 1, 10)) {
			throw new JBFBusinessException("多重度[TORAKU_MAIL_TAJYUDO]=" + this.g_tajyuDo + "です。");

		}

		// 自分の多重番号が、（ＺＭプロパティの）多重度以上の時は、処理終了。
		if (Integer.parseInt(this.g_tajyuNo) >= Integer.parseInt(this.g_tajyuDo)) {
			this.g_log.putTrace("自分の多重番号が、（ＺＭプロパティの）多重度以上のため、処理終了");
			this.isLoopContinue = false;
		}

		// 起動パラメータチェック1のチェックが異常なら終了
		if (!this.isLoopContinue){
			return;}

		// バッチアプリケーション基本情報の取得
		JBAPB32RegistBaseInfo base = new JBAPB32RegistBaseInfo();

		// バッチ起動時の日付・時刻のセット
		this.g_nowDateTime = base.getNowDateTime();

		// 障害-23336（【新会員】抽選結果確認メールが携帯用の文面になっていた）ADD START
		//「ＺＭプロパティ」から携帯のドメインを取得
		mobileDomain = getMobileDomain();
		// 障害-23336（【新会員】抽選結果確認メールが携帯用の文面になっていた）ADD END

		//改善-30866（リアルチケット販売と連動して配信用のオーダーを作成する）ADD START
		ssaRenkeiUketsukeMap = new HashMap<String, String>();
		jcmbssaRenkeiInfoRegist = new JCMBSSARenkeiInfoRegist(this.g_Connection);
		//改善-30866（リアルチケット販売と連動して配信用のオーダーを作成する）ADD END

		this.g_log.putLog("【postStart】処理終了");
	}

	// 3. トランループ前処理(preTrnloop)
	/**
	 * トランループ前処理を行います。<br>
	 *
	 *
	 * @see Z.fw.batch.JBFMain#preTrnloop()
	 */
	protected void preTrnloop() throws JBFSystemException, JBFBusinessException {

		this.g_log.putLog("【preTrnloop】処理開始");

		this.g_log.putLog("【preTrnloop】処理終了");
	}

	// 4-1. ループ制御判定(loopContinueJudge)
	/**
	 * ループの制御を判定します。<br>
	 *
	 * @see Z.fw.batch.JBFMain#loopContinueJudge()
	 */
	protected boolean loopContinueJudge() throws JBFSystemException, JBFBusinessException {

		this.g_log.putLog("【loopContinueJudge】処理開始");

		this.g_log.putTrace(" isLoopContinue [" + this.isLoopContinue + "]");

		this.g_log.putLog("【loopContinueJudge】処理終了");

		// ループ判定フラグの通知
		return this.isLoopContinue;
	}

	// 4-2. コミット実行判定(doCommitJudge)
	/**
	 * コミット実行判定を行います。<br>
	 *
	 *
	 * @see Z.fw.batch.JBFMain#doCommitJudge()
	 */
	protected boolean doCommitJudge() throws JBFSystemException, JBFBusinessException {

		this.g_log.putLog("【doCommitJudge】処理開始");

		// トランザクションループの前処理の記述

		this.g_log.putLog("【doCommitJudge】処理終了");

		return true;
	}

	// 4-3. コミット前処理(preCommit)
	/**
	 * コミット前処理を行います。<br>
	 *
	 *
	 * @see Z.fw.batch.JBFMain#preCommit()
	 */
	protected void preCommit() throws JBFSystemException, JBFBusinessException {

		this.g_log.putLog("【preCommit】処理開始");

		// 該当処理無し

		this.g_log.putLog("【preCommit】処理終了");
	}

	// 4-4. コミット後処理(postCommit)
	/**
	 * コミット後処理を行います。<br>
	 *
	 *
	 * @see Z.fw.batch.JBFMain#postCommit()
	 */
	protected void postCommit() throws JBFSystemException, JBFBusinessException {

		this.g_log.putLog("【postCommit】処理開始");

		// 該当処理無し

		this.g_log.putLog("【postCommit】処理終了");
	}

	// 4-5. トランごとの処理(tranRoutine)
	/**
	 * トランごとの処理を行います。<br>
	 *
	 * @see Z.fw.batch.JBFMain#tranRoutine()
	 */
	protected void tranRoutine() throws JBFSystemException, JBFBusinessException {

		this.g_log.putLog("【tranRoutine】処理開始");

		this.isLoopContinue = false;

		// -------------------------------------
		// 業務開始処理
		// -------------------------------------
		// 各クラスの生成
		tourakuMailInfo =
		new JBAPB32TourakuMailInfo();

		/************************************************************/
		/*															*/
		/* 業務処理 */
		/*															*/
		/************************************************************/
		this.g_log.putTrace("==================================================");
		this.g_log.putTrace(" 1. 当落メール編集元情報の取得");

		// 当落メール編集元情報の取得
		JBAPB32TourakuMailInfoDataBean[] selectDataBean =
		tourakuMailInfo.selectTourakuMailInfo(this.g_Connection, this.g_nowDateTime, this.g_tajyuDo, this.g_tajyuNo);
		int tourakuMailBeanSize = selectDataBean.length;

		this.g_log.putTrace(" 当落メール編集元情報件数 [" + tourakuMailBeanSize + "]");

		// 当落メール編集元情報が無い場合
		if (tourakuMailBeanSize == 0) {

			// 対象データ無しのログ出力
			this.g_log.putTrace("【当落メール編集元情報がありません】：当落メール送信処理終了");
			// メール送信が必要が場合があるため、ここでは終了しない

		} else {

			// ＢＴチケット販売サブ用定数のデータを抽出する。
			getDataBTSubTeisu();
		}

		// コミット件数
		long commitNum = msg.getCommitNum();

		this.g_log.putTrace("==================================================");

		// 当落メール編集、送信、ＤＢ更新
		for (int mailCount = 0; mailCount < tourakuMailBeanSize; mailCount++) {

			try {

				if (tourakuMailInfo.lockTourakuMailInfo(this.g_Connection, selectDataBean[mailCount]) == 1) {
					// 排他正常の場合

					// 当落結果のメール送信処理
					this.sendTourakuMail(selectDataBean[mailCount]);

					// 当落メール編集元情報の更新を行います
					this.updateTourakuMail(this.g_Connection, selectDataBean[mailCount]);
				}

				selectDataBean[mailCount] = null;

			} catch (Exception e) {
				JCMLog.debugLog(JCMBConstants.DEBUG_LOG_LEVEL, "※◆当落メール業務処理時異常発生しました。◆※" + e.toString());
				JCMLog.errorLog(JCMBConstants.ERROR_LOG_LEVEL, "MB1901E", "当落メール処理時、異常が発生しました。<BR>", e);
				e.printStackTrace();

			}

			// 処理済件数％バッチプロパティのコミット件数＝0、または最終レコードの場合
			if ((mailCount + 1) % commitNum == 0 || mailCount == tourakuMailBeanSize - 1) {
				this.g_log.putDetail("コミット：" + (mailCount + 1) + "件(処理済)／" + tourakuMailBeanSize + "件(ＡＬＬ)");
				commitTransaction();
			}
		}

		this.g_log.putDetail("プレ当選メール：" + g_preTousenMailNum + "件");
		this.g_log.putDetail("プレ落選メール：" + g_preRakusenMailNum + "件");
		this.g_log.putDetail("先プレ当選メール：" + g_sakibaraiPreTousenMailNum + "件");
		this.g_log.putDetail("先プレ落選メール：" + g_sakibaraiPreRakusenMailNum + "件");

		// 取得当落メール編集元情報のアラートメール送信処理呼出
		this.alertTourakuMail(this.g_Connection);

		this.g_log.putLog("【tranRoutine】処理終了");

	}

	// 5. 正常終了前処理(preNormalEnd)
	/**
	 * 正常終了前処理を行います。<br>
	 *
	 * @see Z.fw.batch.JBFMain#preNormalEnd()
	 */
	protected void preNormalEnd() throws JBFSystemException, JBFBusinessException {

		this.g_log.putLog("【preNormalEnd】処理開始");

		// 完了コード(正常終了)の設定
		msg.setExitCode(JBAPB32Constants.BATCH_END_CODE_NORMAL);

		this.g_log.putLog("【preNormalEnd】処理終了");
	}

	// 6. 業務異常終了前処理(preBusinessAbnormalEnd)
	/**
	 * 業務異常終了前処理を行います。<br>
	 *
	 * @see Z.fw.batch.JBFMain#preBusinessAbnormalEnd(Exception)
	 */
	protected void preBusinessAbnormalEnd(Exception arg0) {

		this.g_log.putLog("【preBusinessAbnormalEnd】処理開始");

		// 完了メッセージの設定
		msg.setOutMsg(JBAPB32Constants.BATCH_END_CODE_BUSINESS_ERR + " " + "業務異常終了前処理");

		// 完了コード(業務異常(致命的エラー))の設定
		msg.setExitCode(JBAPB32Constants.BATCH_END_CODE_BUSINESS_ERR);

		this.g_log.putLog("【preBusinessAbnormalEnd】処理終了");
	}

	// 7. システム異常終了前処理(preSystemAbnormalEnd)
	/**
	 * システム異常終了前処理を行います。<br>
	 *
	 * @see Z.fw.batch.JBFMain#preSystemAbnormalEnd(Throwable)
	 */
	protected void preSystemAbnormalEnd(Throwable arg0) {

		this.g_log.putLog("【preSystemAbnormalEnd】処理開始");

		// 完了メッセージの設定
		msg.setOutMsg(JBAPB32Constants.BATCH_END_CODE_SYSTEM_ERR + " " + "システム異常終了前処理");

		// 完了コード(システム異常終了)の設定
		msg.setExitCode(JBAPB32Constants.BATCH_END_CODE_SYSTEM_ERR);

		this.g_log.putLog("【preSystemAbnormalEnd】処理終了");
	}

	// 8. メイン処理
	/**
	 * バッチ制御処理クラスのインスタンスを生成します。<br>
	 *
	 * @see Z.fw.batch.JBFMain#main(String[] args)
	 */
	public static void main(String[] args) {

		// バッチクラスの生成
		new JBAPB3204(args);
	}

	/*
	 * 内部メソッドの記述部(非公開)
	 */

	/**
	 * 当落メールの送信を行います。<br>
	 *
	 * @param mailDataBean
	 *           当落メール編集元情報Bean<br>
	 */
	private void sendTourakuMail(JBAPB32TourakuMailInfoDataBean mailDataBean) {

		// sendTourakuMail開始ログ出力
		this.g_log.putLog("【sendTourakuMail】処理開始");

		// メール送信結果
		// boolean mailSendResult = false;

		// メール種別番号
		short mailSyubetsuNo = -1;

		String strGyoumu = "";

		// オーダー区分がプレの場合
		if (mailDataBean.orderKBN.equals(JOABConstants.ORDERKUBUN_PURE)) {
			// 当選メール
			if (mailDataBean.tourakukekkaKBN.equals(JOABConstant.ORDERSTATUS_TOUSEN)) {
				// メール処理クラスの生成(種別毎に宣言）

				mailSyubetsuNo = JCMBConstants.PRE_TOUSEN_MAIL;
				strGyoumu = "プレ当選メール業務";

				this.g_preTousenMailNum++;

				// 落選メール
			} else if (mailDataBean.tourakukekkaKBN.equals(JOABConstant.ORDERSTATUS_RAKUSEN)) {
				// メール処理クラスの生成(種別毎に宣言）

				mailSyubetsuNo = JCMBConstants.PRE_RAKUSEN;
				strGyoumu = "プレ落選メール業務";
				this.g_preRakusenMailNum++;

			}
		}

		// オーダー区分が先払いプレの場合
		if (mailDataBean.orderKBN.equals(JOABConstants.ORDERKUBUN_SAKIFUTSUPURE)) {
			// 当選メール
			if (mailDataBean.tourakukekkaKBN.equals(JOABConstant.ORDERSTATUS_TOUSEN)) {
				// メール処理クラスの生成(種別毎に宣言）

				mailSyubetsuNo = JCMBConstants.SAKIBARAI_PRE_TOUSEN_MAIL;
				strGyoumu = "先払いプレ当選メール業務";
				this.g_sakibaraiPreTousenMailNum++;

				// 落選メール
			} else if (mailDataBean.tourakukekkaKBN.equals(JOABConstant.ORDERSTATUS_RAKUSEN)) {
				// メール処理クラスの生成(種別毎に宣言）

				mailSyubetsuNo = JCMBConstants.SAKIBARAI_PRE_RAKUSEN_MAIL;
				strGyoumu = "先払いプレ落選メール業務";
				this.g_sakibaraiPreRakusenMailNum++;

			}
		}

		JCMLog.debugLog(JCMBConstants.DEBUG_LOG_LEVEL, "◆" + strGyoumu + "Control START");
		JCMLog.debugLog(JCMBConstants.DEBUG_LOG_LEVEL, "◆管理番号 = " + mailDataBean.kanriNO);

		if (mailSyubetsuNo > -1) {
			try {
				// プレ当選メール処理を行う
				mailAMC(String.valueOf(mailDataBean.kanriNO), mailSyubetsuNo);

				mailDataBean.sousinsumiFLG = "1";

				//改善-30866（リアルチケット販売と連動して配信用のオーダーを作成する）ADD START
				if (mailSyubetsuNo == JCMBConstants.PRE_TOUSEN_MAIL) {
					doSSARenkei(mailDataBean);
				}
				//改善-30866（リアルチケット販売と連動して配信用のオーダーを作成する）ADD END

			} catch (Exception e) {
				JCMLog.debugLog(JCMBConstants.DEBUG_LOG_LEVEL, "※◆当落メール業務処理時異常発生しました。◆※");
				errControl(String.valueOf(mailSyubetsuNo), strGyoumu, e.toString());
				e.printStackTrace();
			}

			JCMLog.debugLog(JCMBConstants.DEBUG_LOG_LEVEL, "◆プレ当選メール Control END");

		}

		// 送信依頼のパラメータ出力(デバッグ)
		JBAPB32LogUtil.putDebugLog("【送信依頼用パラメータ】");
		JBAPB32LogUtil.putDebugLog(" 興行コード [" + mailDataBean.kougyoCD + "]");
		JBAPB32LogUtil.putDebugLog(" 興行サブコード [" + mailDataBean.kougyoSubCD + "]");
		JBAPB32LogUtil.putDebugLog(" 受付情報コード [" + mailDataBean.uketsukeInfoCD + "]");
		JBAPB32LogUtil.putDebugLog(" 管理番号 [" + mailDataBean.kanriNO + "]");
		JBAPB32LogUtil.putDebugLog(" 当落結果区分 [" + mailDataBean.tourakukekkaKBN + "]");
		JBAPB32LogUtil.putDebugLog(" オーダー区分 [" + mailDataBean.orderKBN + "]");
		JBAPB32LogUtil.putDebugLog(" メールテンプレートＩＤ [" + mailDataBean.templateID + "]");
		JBAPB32LogUtil.putDebugLog(" 送信フラグ [" + mailDataBean.sousinsumiFLG + "]");

		// sendTourakuMail終了ログ出力
		this.g_log.putLog("【sendTourakuMail】処理終了");

	}

	/**
	 * 改善-30866（リアルチケット販売と連動して配信用のオーダーを作成する）ADD
	 *
	 * SSA連携情報登録を行います。<br>
	 *
	 * @param mailDataBean
	 *            当落メール編集元情報Bean<br>
	 */
	private void doSSARenkei(JBAPB32TourakuMailInfoDataBean mailDataBean) throws JBFSystemException, JBFBusinessException {

		// 開始ログ出力
		this.g_log.putLog("【doSSARenkei】処理開始");

		ArrayList argList = new ArrayList();
		ArrayList resultList = new ArrayList();

		argList.add(mailDataBean.kanriNO);

		// 入金ステータスを取得
		try {
			resultList = JCMDbAccessCommon.select("SB00277", argList, this.g_Connection);
		} catch (Exception e) {
			throw new JBFBusinessException(e.toString(), e);
		}

		if (resultList.size() > 0) {
			HashMap resultMap = (HashMap)resultList.get(0);
			String nyukinStatus = JCMBUtilityCommon.objToStr(resultMap.get("入金ステータス"));
			//障害-31961（抽選受付でSSA連携できない場合がある）Mod Start
			//連携対象とする入金ステータスの追加
			/**入金ステータス 11：入金済
				入金ステータス 12：入金済（別途精算）
				入金ステータス 21：売上済
				入金ステータス 22：売上済（別途精算）
				入金ステータス 31：経理売上済
				入金ステータス 32：経理売上済（別途精算）	*/

			//if (nyukinStatus.equals(JCMBConstants.NYUKIN_STATUS_NYUKINSUMI)) {

			if (nyukinStatus.equals(JCMBConstants.NYUKIN_STATUS_NYUKINSUMI)
				|| nyukinStatus.equals(JCMBConstants.NYUKIN_STATUS_NYUKINSUMI_BETTOSEISAN)
				|| nyukinStatus.equals(JCMBConstants.NYUKIN_STATUS_URIAGESUMI)
				|| nyukinStatus.equals(JCMBConstants.NYUKIN_STATUS_URIAGESUMI_BETTOSEISAN)
				|| nyukinStatus.equals(JCMBConstants.NYUKIN_STATUS_KEIRIURIAGESUMI)
				|| nyukinStatus.equals(JCMBConstants.NYUKIN_STATUS_KEIRIURIAGESUMI_BETTOSEISAN)
				){

				//障害-31961（抽選受付でSSA連携できない場合がある）Mod End

				//受付がリアル配信連携対象であるか判定するフラグ
				String strRenkei = JCMBSSARenkeiInfoRegist.PROC_CODE_NOT_RENKEI;

				//チェック済みの受付か、mapより判定
				//mapを確認、キーで結果取得できなかったら判定処理を行う＋mapに結果追加
				if(!ssaRenkeiUketsukeMap.containsKey(mailDataBean.kougyoCD + mailDataBean.kougyoSubCD + mailDataBean.uketsukeInfoCD)) {
					//処理を呼び出した時はmapにキーと
					strRenkei = jcmbssaRenkeiInfoRegist.checkRenkeiUketsukeInfo(mailDataBean.kougyoCD, mailDataBean.kougyoSubCD, mailDataBean.uketsukeInfoCD);
					//チェック済みの受付はmapに追加
					ssaRenkeiUketsukeMap.put(mailDataBean.kougyoCD + mailDataBean.kougyoSubCD + mailDataBean.uketsukeInfoCD, strRenkei);
				} else {
					//チェック済みの場合は連携対象外
					strRenkei = ssaRenkeiUketsukeMap.get(mailDataBean.kougyoCD + mailDataBean.kougyoSubCD + mailDataBean.uketsukeInfoCD);
				}
				JCMLog.debugLog(JCMBConstants.DEBUG_LOG_LEVEL,"SSA連携受付情報判定結果："+strRenkei);

				if (strRenkei.equals(JCMBSSARenkeiInfoRegist.PROC_CODE_NORMAL)) {
					//管理番号、renkeiフラグを引数に、createRenkeiInfo【SSA連携情報作成】を呼び出し、実行結果をコードで取得
					String retCd = jcmbssaRenkeiInfoRegist.createRenkeiInfo(Integer.parseInt(mailDataBean.kanriNO), false);

					if (JCMBSSARenkeiInfoRegist.PROC_CODE_ERR_NO_INFO.equals(retCd)) {
						//SSA登録依頼処理："連携情報作成異常（データ整合性エラー）"の場合
						JCMLog.errorLog(JCMBConstants.ERROR_LOG_LEVEL, "", "91：SSA連携情報作成異常（データ整合性エラー） 管理番号：" + mailDataBean.kanriNO, null);
					}else if(JCMBSSARenkeiInfoRegist.PROC_CODE_ERR_RENKEI.equals(retCd)) {
						//SSA登録依頼処理："連携情報作成異常（業務エラー）"の場合
						JCMLog.errorLog(JCMBConstants.ERROR_LOG_LEVEL, "", "92：SSA連携情報作成異常（業務エラー） 管理番号：" + mailDataBean.kanriNO, null);
					}else if(JCMBSSARenkeiInfoRegist.PROC_CODE_ERR_ETC.equals(retCd)) {
						//SSA登録依頼処理："予期しない処理異常（システムエラー）"の場合
						JCMLog.errorLog(JCMBConstants.ERROR_LOG_LEVEL, "", "99：SSA連携情報作成異常（予期しないエラー） 管理番号：" + mailDataBean.kanriNO, null);
					}
				}
			}
		}

		// 終了ログ出力
		this.g_log.putLog("【doSSARenkei】処理終了");
	}

	/**
	 * 当落メール編集元情報の更新を行います。<br>
	 *
	 * @param connection
	 *            DBコネクション
	 * @param mailDataBean
	 *            当落メール編集元情報Bean<br>
	 * @return 送信済みフラグ 0：未送信,1：送信済み<br>
	 */
	private String updateTourakuMail(Connection connection, // コネクション
			JBAPB32TourakuMailInfoDataBean mailDataBean) throws JBFSystemException, JBFBusinessException {

		// updateTourakuMail開始ログ出力
		this.g_log.putLog("【updateTourakuMail】処理開始");

		// 送信依頼のパラメータ出力(デバッグ)
		JBAPB32LogUtil.putDebugLog("【送信依頼用パラメータ】");
		JBAPB32LogUtil.putDebugLog(" 興行コード [" + mailDataBean.kougyoCD + "]");
		JBAPB32LogUtil.putDebugLog(" 興行サブコード [" + mailDataBean.kougyoSubCD + "]");
		JBAPB32LogUtil.putDebugLog(" 受付情報コード [" + mailDataBean.uketsukeInfoCD + "]");
		JBAPB32LogUtil.putDebugLog(" 管理番号 [" + mailDataBean.kanriNO + "]");
		JBAPB32LogUtil.putDebugLog(" 当落結果区分 [" + mailDataBean.tourakukekkaKBN + "]");
		JBAPB32LogUtil.putDebugLog(" オーダー区分 [" + mailDataBean.orderKBN + "]");
		JBAPB32LogUtil.putDebugLog(" メールテンプレートＩＤ [" + mailDataBean.templateID + "]");
		JBAPB32LogUtil.putDebugLog(" 送信フラグ [" + mailDataBean.templateID + "]");

		// 送信結果の更新
		int updateNum = tourakuMailInfo.updateTourakuMailSendStatus(connection, mailDataBean);
		this.g_log.putTrace(" 更新件数 [" + updateNum + "]");

		// updateTourakuMail終了ログ出力
		this.g_log.putLog("【updateTourakuMail】処理終了");

		// メール送信結果の通知
		return mailDataBean.sousinsumiFLG;
	}

	/**
	 * アラートメールの送信を行います。<br>
	 *
	 * @param connection
	 *            DBコネクション
	 */
	private void alertTourakuMail(Connection connection
	) throws JBFSystemException, JBFBusinessException {

		// alertTourakuMail開始ログ出力
		this.g_log.putLog("【alertTourakuMail】処理開始");

		// 送信結果の更新
		JBAPB32TourakuMailInfoDataBean[] selectDataBean = tourakuMailInfo.selectTourakuMailInfo4alert(
				this.g_Connection, this.g_nowDateTime, this.g_tajyuDo, this.g_tajyuNo);
		int tourakuMailBeanSize = selectDataBean.length;

		this.g_log.putTrace(" アラートメール送信情報件数 [" + tourakuMailBeanSize + "]");

		// アラートメール送信処理
		if (tourakuMailBeanSize > 0) {
			try {

				// 当落メール用コンストラクタの設定
				JCMBAlertMailRap rap = new JCMBAlertMailRap(JBAPB32Constants.ALERT4TOURAKUMAIL);

				// アラートメールの内容表示設定
				rap.collectGivingInfo(" 未送信件数 [" + tourakuMailBeanSize + "]");

				// アラートメール送信
				rap.sendAlertMail();

			} catch (Exception ex) {

				this.g_log.putException(" アラートメールは送信されませんでした。", ex);
			}
		}

		this.g_log.putLog("【alertTourakuMail】処理終了");

		return;
	}

	/**
	 * ＢＴチケット販売サブ用定数のデータを抽出する。
	 *
	 * @param なし
	 * <BR>
	 * @throws JBFBusinessException
	 */
	private void getDataBTSubTeisu() throws JBFBusinessException {
		ArrayList argList = new ArrayList();
		ArrayList resultArray = new ArrayList();

		// ＢＴチケット販売サブ用定数の取得
		try {
			resultArray = JCMDbAccessCommon.select("SB00061", argList, this.g_Connection);
		} catch (Exception e) {
			throw new JBFBusinessException(e.toString(), e);
		}

		if (resultArray == null || resultArray.size() == 0) {
			JCMLog.debugLog(JCMBConstants.DEBUG_LOG_LEVEL, "★★★★ ＢＴチケット販売サブ用定数 データなし ★★★★");
			return;
		}
		HashMap map = (HashMap) resultArray.get(0);
		strBtTicketHanvaiSubTeisu = new String[5];
		// 配送予定文言
		strBtTicketHanvaiSubTeisu[0] = JCMBUtilityCommon.objToStr(map.get("配送予定文言"));
		// 先着支払期限時刻
		strBtTicketHanvaiSubTeisu[1] = JCMBUtilityCommon.objToStr(map.get("先着支払期限時刻"));
		// 発券開始時刻
		strBtTicketHanvaiSubTeisu[2] = JCMBUtilityCommon.objToStr(map.get("発券開始時刻（コンビニ）"));
		// コンビニ発券終了日
		strBtTicketHanvaiSubTeisu[3] = JCMBUtilityCommon.objToStr(map.get("コンビニ発券終了日"));
		// コンビニ発券終了時刻（案内）
		strBtTicketHanvaiSubTeisu[4] = JCMBUtilityCommon.objToStr(map.get("コンビニ発券終了時刻（案内）"));

		return;

	}

	/**
	 * メール送信機能のエントリメソッドです。<br>
	 * 区分を取得して、業務共通メール部品を呼出します。<br>
	 *
	 * @param kanriNo
	 *            管理番号（必須）
	 * @param syubetsuNo
	 *            メール種別
	 */
	public void mailAMC(String kanriNo, short syubetsuNo) throws Exception {

		long beginTime = JCMLog.beginBenchmarkLog(JCMBConstants.TRACE_LOG_LEVEL
				, "mail性能テスト_mailメイン処理START_管理番号【" + kanriNo + "】 メール種別【" + syubetsuNo + "】");
		// JCMLog.endBenchmarkLog(JCMBConstants.TRACE_LOG_LEVEL,"mail性能テスト_mailメイン処理START",beginTime);
		String strSeinoTime = JCMBUtilityCommon.getStrTrueSystemDate();
		JCMLog.traceLog(JCMBConstants.TRACE_LOG_LEVEL, "mailAMC", "", "mail性能テスト_mailメイン処理Start_管理番号【" + kanriNo
				+ "】 メール種別【" + syubetsuNo + "】:" + strSeinoTime);
		try {

			JCMLog.debugLog(JCMBConstants.DEBUG_LOG_LEVEL, "◇　start");
			JCMLog.debugLog(JCMBConstants.DEBUG_LOG_LEVEL, "◆管理番号=" + kanriNo);
			JCMLog.debugLog(JCMBConstants.DEBUG_LOG_LEVEL, "◆メール種別=" + syubetsuNo);
			// メール種別
			String strMailsyubetsu = String.valueOf(syubetsuNo);

			if (strMailsyubetsu.length() < 2) {
				strMailsyubetsu = "0" + strMailsyubetsu;
			}
			// 置換情報HashMap値を設定する
			JCMBSendMailEditItem editItem = new JCMBSendMailEditItem(this.g_Connection, this.strBtTicketHanvaiSubTeisu);

			JCMBSendMailGetDBInfo getDBInfo = new JCMBSendMailGetDBInfo(this.g_Connection);

			JCMBSendMailOrderBean dbBean = getDBInfo.getData(kanriNo, null, null, null, getTemplateInfo(strMailsyubetsu)
					.getArySqlNo());

			// ＢＴオーダーの「オーダー非公開フラグ」が非公開の場合は、メールを送信しない。
			String strOrderHikoukaiFlg = dbBean.getOOrderHikoukaiFlg();
			if (strOrderHikoukaiFlg.equals("1")) {
				JCMLog.debugLog(JCMBConstants.DEBUG_LOG_LEVEL, "オーダーが非公開のためメール送信対象外 メール種別：" + syubetsuNo + "　管理番号："
						+ kanriNo);
				return;
			}

			// 呼出し元メール種別を設定する
			editItem.setRealMailType(strMailsyubetsu);

			// 区分を取得します
			String kubun = logicAMC(editItem, kanriNo, strMailsyubetsu, dbBean);
			
			// 言語コードを取得します
			String language_code = dbBean.getLanguage_code();
			
			// インバウンド判定
			boolean ibt_flg = false;
			// 言語コードがブランクでない場合
			if (language_code != "") {
				// ＺＭプロパティよりインバウンド対象のメール種別を取得
				String ibtSyubetsuNo = JCMCommonProperties.getProperty( "IBT_MAIL_SYUBETSU" );
				
				// カンマ区切りでプロパティを分割し配列に格納
				String[] ibtSyubetsuNoList = ibtSyubetsuNo.split(",");
				
				// パラメーターのメール種別がインバウンド対象のメール種別と一致する場合
				if(Arrays.asList(ibtSyubetsuNoList).contains(String.valueOf(syubetsuNo))){
					ibt_flg = true;
		          }
			}
			String kaiinItikenKbn = dbBean.getOKainIkkenKbn();
			String strkaiinIId = dbBean.getOKainId();
			if (strkaiinIId.equals("")) {
				strkaiinIId = "一見";
			}

			// スキン変更HashMap
			mapSkinInfo = editItem.getMailSkinMap(dbBean, strMailsyubetsu, kubun, 0, null);

			// 会員情報の取得
			HashMap infoMap = getPersonInfo(dbBean);
			String strHukamail = "";
			// 会員の場合
			if (("1".equals(kaiinItikenKbn))) {

				// 障害-23336（【新会員】抽選結果確認メールが携帯用の文面になっていた）ADD START
				JCMBCheckCommon chkCommon = new JCMBCheckCommon(this.g_Connection);
				// 新会員の場合
				if (chkCommon.isShinKaiinJoho(dbBean.getOSosikiCd(), dbBean.getOKainId())) {
					//メールアドレス1
					String mailType = JCMBUtilityCommon.objToStr(infoMap.get("メールアドレス1キャリア種別"));
					if((!"".equals(JCMBUtilityCommon.objToStr(infoMap.get("メールアドレス1ドメイン"))))
						&&(! "".equals(JCMBUtilityCommon.objToStr(infoMap.get("メールアドレス1"))))
						&&(! "1".equals(JCMBUtilityCommon.objToStr(infoMap.get("メールアドレス1不達フラグ"))))){

						// PC用メールアドレス・携帯キャリアどちらにおいてもに、PCメール用テンプレートを使用する
						// 区分より必須置換文字用HashMap
						mapTikanMonziPc = editItem.getMailTikanMap(dbBean, getTemplateInfo(strMailsyubetsu)
								.getMapTikanMonzi_Tuzyo(), strMailsyubetsu, kubun, true);

						// 区分より必須差込文句用HashMap
						mapSasikomiMonku = editItem.getMailSashiMap(dbBean, getTemplateInfo(strMailsyubetsu)
								.getMapSasikomiMonku_Tuzyo(), strMailsyubetsu, kubun, true);


						String mailAddr = (String)infoMap.get("メールアドレス1");
						if(ibt_flg) {
							// インバウンド受付の場合
							mailSend("1", mailAddr, kanriNo, kubun, strMailsyubetsu, infoMap, language_code);
						}else {
							//それ以外の場合
							mailSend("1", mailAddr, kanriNo, kubun, strMailsyubetsu, infoMap, "");
						}

					}else{
						strHukamail = "【メールアドレス1】";
					}

					//メールアドレス2
					mailType = JCMBUtilityCommon.objToStr(infoMap.get("メールアドレス2キャリア種別"));
					if((!"".equals(JCMBUtilityCommon.objToStr(infoMap.get("メールアドレス2ドメイン"))))
						&&(! "".equals(JCMBUtilityCommon.objToStr(infoMap.get("メールアドレス2"))))
						&&(! "1".equals(JCMBUtilityCommon.objToStr(infoMap.get("メールアドレス2不達フラグ"))))){

						// PC用メールアドレス・携帯キャリアどちらにおいてもに、PCメール用テンプレートを使用する
						// 区分より必須置換文字用HashMap
						mapTikanMonziPc = editItem.getMailTikanMap(dbBean, getTemplateInfo(strMailsyubetsu)
								.getMapTikanMonzi_Tuzyo(), strMailsyubetsu, kubun, true);

						// 区分より必須差込文句用HashMap
						mapSasikomiMonku = editItem.getMailSashiMap(dbBean, getTemplateInfo(strMailsyubetsu)
								.getMapSasikomiMonku_Tuzyo(), strMailsyubetsu, kubun, true);

						String mailAddr = (String)infoMap.get("メールアドレス2");
						if(ibt_flg) {
							// インバウンド受付の場合
							mailSend("1", mailAddr, kanriNo, kubun, strMailsyubetsu, infoMap, language_code);
						}else {
							//それ以外の場合
							mailSend("1", mailAddr, kanriNo, kubun, strMailsyubetsu, infoMap, "");
						}

					}else{
						strHukamail = strHukamail +"【メールアドレス2】";
					}

				} else {
				// 障害-23336（【新会員】抽選結果確認メールが携帯用の文面になっていた）ADD END

					// PCメール
					if ((!"".equals(JCMBUtilityCommon.objToStr(infoMap.get("ＰＣメールアドレス＿ドメイン"))))
							&& (!"".equals(JCMBUtilityCommon.objToStr(infoMap.get("ＰＣメールアドレス"))))
							&& (!"1".equals(JCMBUtilityCommon.objToStr(infoMap.get("ＰＣメール不達フラグ"))))
							&& "1".equals(JCMBUtilityCommon.objToStr(infoMap.get("ＰＣメールアドレス送信要否")))) {

						String mailAddr = (String) infoMap.get("ＰＣメールアドレス");

						// 区分より必須置換文字用HashMap
						mapTikanMonziPc = editItem.getMailTikanMap(dbBean, getTemplateInfo(strMailsyubetsu)
								.getMapTikanMonzi_Tuzyo(), strMailsyubetsu, kubun, true);

						// 区分より必須差込文句用HashMap
						mapSasikomiMonku = editItem.getMailSashiMap(dbBean, getTemplateInfo(strMailsyubetsu)
								.getMapSasikomiMonku_Tuzyo(), strMailsyubetsu, kubun, true);
						if(ibt_flg) {
							// インバウンド受付の場合
							mailSend("1", mailAddr, kanriNo, kubun, strMailsyubetsu, infoMap, language_code);
						}else {
							//それ以外の場合
							mailSend("1", mailAddr, kanriNo, kubun, strMailsyubetsu, infoMap, "");
						}

					} else {
						strHukamail = "【PC】";
					}
					// 携帯メール
					if ((!"".equals(JCMBUtilityCommon.objToStr(infoMap.get("携帯メールドメイン"))))
							&& (!"".equals(JCMBUtilityCommon.objToStr(infoMap.get("携帯メールアドレス"))))
							&& (!"1".equals(JCMBUtilityCommon.objToStr(infoMap.get("携帯メール不達フラグ"))))
							&& "1".equals(JCMBUtilityCommon.objToStr(infoMap.get("携帯メールアドレス送信要否")))) {;

						// 区分より必須置換文字用HashMap
						mapTikanMonziKeitai = editItem.getMailTikanMap(dbBean, getTemplateInfo(strMailsyubetsu)
								.getMapTikanMonzi_Tuzyo(), strMailsyubetsu, kubun, false);

						// 区分より必須差込文句用HashMap
						mapSasikomiMonkuKeitai = editItem.getMailSashiMap(dbBean, getTemplateInfo(strMailsyubetsu)
								.getMapSasikomiMonku_Tuzyo(), strMailsyubetsu, kubun, false);

						String mailAddr = (String) infoMap.get("携帯メールアドレス");
						
						if(ibt_flg) {
							// インバウンド受付の場合
							mailSend("2", mailAddr, kanriNo, kubun, strMailsyubetsu, infoMap, language_code);
						}else {
							//それ以外の場合
							mailSend("2", mailAddr, kanriNo, kubun, strMailsyubetsu, infoMap, "");
						}

					} else {
						strHukamail = strHukamail + "【携帯】";
					}
				}
				// 一見の場合
			} else {
				// PCメール
				if (!"".equals(objToStr(infoMap.get("ＰＣメールアドレス")))) {
					String mailAddr = (String) infoMap.get("ＰＣメールアドレス");
					// "◆ＰＣメールアドレス:　" + mailAddr);

					// 区分より必須置換文字用HashMap
					mapTikanMonziPc = editItem.getMailTikanMap(dbBean, getTemplateInfo(strMailsyubetsu)
							.getMapTikanMonzi_Tuzyo(), strMailsyubetsu, kubun, true);

					// 区分より必須差込文句用HashMap
					mapSasikomiMonku = editItem.getMailSashiMap(dbBean, getTemplateInfo(strMailsyubetsu)
							.getMapSasikomiMonku_Tuzyo(), strMailsyubetsu, kubun, true);
					if(ibt_flg) {
						// インバウンド受付の場合
						mailSend("1", mailAddr, kanriNo, kubun, strMailsyubetsu, infoMap, language_code);
					}else {
						//それ以外の場合
						mailSend("1", mailAddr, kanriNo, kubun, strMailsyubetsu, infoMap, "");
					}

				} else {
					strHukamail = "【PC】";
				}

				// 携帯メール
				// 携帯メールにおいても、PCメール用テンプレートを使用するように変更
				if (!"".equals(objToStr(infoMap.get("携帯メールアドレス")))) {
					String mailAddr = (String) infoMap.get("携帯メールアドレス");

					// 区分より必須置換文字用HashMap
					mapTikanMonziPc = editItem.getMailTikanMap(dbBean, getTemplateInfo(strMailsyubetsu)
							.getMapTikanMonzi_Tuzyo(), strMailsyubetsu, kubun, true);

					// 区分より必須差込文句用HashMap
					mapSasikomiMonku = editItem.getMailSashiMap(dbBean, getTemplateInfo(strMailsyubetsu)
							.getMapSasikomiMonku_Tuzyo(), strMailsyubetsu, kubun, true);
					
					if(ibt_flg) {
						// インバウンド受付の場合
						mailSend("1", mailAddr, kanriNo, kubun, strMailsyubetsu, infoMap, language_code);
					}else {
						//それ以外の場合
						mailSend("1", mailAddr, kanriNo, kubun, strMailsyubetsu, infoMap, "");
					}
					
				} else {
					strHukamail = strHukamail + "【携帯】";
				}
			}

			if (!strHukamail.equals("")) {
				JCMLog.traceLog(JCMBConstants.TRACE_LOG_LEVEL, "mailAMC", "", strHukamail + "メールは送信対象外　会員ID【"
						+ strkaiinIId + "】");
			}

			JCMLog.debugLog(JCMBConstants.DEBUG_LOG_LEVEL, "◇　end");
		} catch (Exception e) {
			String message = JCMMessageManager.getMessage("MB1903E") + "：送信異常";
			JCMLog.errorLog(JCMBConstants.ERROR_LOG_LEVEL, "MB1903E", message + " 管理番号["+kanriNo+"]", e);
			throw new JCFException(e, "MB1903E", message);
		}

		JCMLog.endBenchmarkLog(JCMBConstants.TRACE_LOG_LEVEL, "mail性能テスト_mailメイン処理End", beginTime);
		strSeinoTime = JCMBUtilityCommon.getStrTrueSystemDate();
		JCMLog.traceLog(JCMBConstants.TRACE_LOG_LEVEL, "mailAMC", "", "mail性能テスト_mailメイン処理END_										:"
				+ strSeinoTime);
	}

	/**
	 * メール送信処理
	 *
	 * @param mailDiv
	 *            メール区分（1:PC、2:携帯）
	 * @param mailAddr
	 *            メールアドレス
	 * @param kanriNo
	 *            管理番号
	 * @param kubun
	 *            区分
	 * @param mailType
	 *            メール種別
	 * @param infoMap
	 *            会員情報Map
	 * @param language_code
	 * 			  言語コード          
	 */
	private void mailSend(String mailDiv, String mailAddr, String kanriNo, String kubun, String mailType,
			HashMap infoMap, String language_code) throws Exception {

		// メールのサブジェクト（件名）の取得
		String mailSubject = "";
		String key = "";
		
		if ( mailDiv.equals("1") ){
			key = "MAIL_SUBJECT_" + mailType;
		}else{
			key = "MAIL_SUBJECT_" + mailType + "_K";
		}
			
		if(language_code!="") {
			key = "MAIL_SUBJECT_" + mailType + "_" +language_code;
		}
		
		JCMLog.debugLog(JCMBConstants.DEBUG_LOG_LEVEL, "◆MAIL_SUBJECT_Property=" + key);
		mailSubject = JCMCommonProperties.getProperty(key);
		JCMLog.debugLog(JCMBConstants.DEBUG_LOG_LEVEL, "◆MAIL_SUBJECT_=" + mailSubject);
		
		if(language_code!="") {
			// 個別メール(インバウンドメールの場合はPCメールで固定)
			realMail(
					"1", mailAddr, kanriNo, mailType, infoMap, mailSubject, language_code);
		}else {
			
			// 個別メール
			realMail(
					mailDiv, mailAddr, kanriNo, mailType, infoMap, mailSubject, language_code);
		}
	}

	/**
	 * 個別メール送信処理
	 * JCMBSendMailCommon commonMailをrealMailのように修正しました。
	 * @param mailDiv
	 *            メール区分（1:PC、2:携帯）
	 * @param mailAddr
	 *            メールアドレス
	 * @param kanriNo
	 *            管理番号
	 * @param mailType
	 *            メール種別
	 * @param infoMap
	 *            会員情報Map
	 * @param mailSubject
	 *            メールタイトル
	 * @param language_code
	 * 			  言語コード
	 */
	private void realMail(String mailDiv, String mailAddr, String kanriNo, String mailType, HashMap infoMap,
			String mailSubject, String language_code) throws Exception {

		// 業務共通メール部品を呼出す
		JCMBSendMailWeb sendMail = new JCMBSendMailWeb();

		// 送信先メールアドレス
		sendMail.setSendMailInfo("MAILADDRESS", mailAddr);
		// 管理番号
		sendMail.setSendMailInfo("KANRI_NO", kanriNo);
		// 送信区分
		sendMail.setSendMailInfo("SEND_DIVISION", mailDiv);
		// メール種別
		sendMail.setSendMailInfo("MAIL_TYPE", mailType);
		// 送信者
		String mailFrom = "";
		// 送信元メールアドレス
		String mailFromAdd = "";
		// 送信者
		sendMail.setSendMailInfo("MAILFORM", mailFrom);
		// 送信元メールアドレス
		sendMail.setSendMailInfo("MAILFROMADD", mailFromAdd);
		// 組織コード
		sendMail.setSendMailInfo("ORGANIZATION_CODE", JCMBUtilityCommon.objToStr(infoMap.get("組織コード")));
		// 会員ID
		sendMail.setSendMailInfo("MEMBER_ID", JCMBUtilityCommon.objToStr(infoMap.get("会員ＩＤ")));
		// カナ氏名（姓）
		sendMail.setSendMailInfo("KANANAME_SEI", JCMBUtilityCommon.objToStr(infoMap.get("カナ氏名＿姓")));
		// カナ氏名（名）
		sendMail.setSendMailInfo("KANANAME_MEI", JCMBUtilityCommon.objToStr(infoMap.get("カナ氏名＿名")));
		// 漢字氏名（姓）
		sendMail.setSendMailInfo("KANJINAME_SEI", JCMBUtilityCommon.objToStr(infoMap.get("漢字氏名＿姓")));
		// 漢字氏名（名）
		sendMail.setSendMailInfo("KANJINAME_MEI", JCMBUtilityCommon.objToStr(infoMap.get("漢字氏名＿名")));
		// 受付番号
		sendMail.setSendMailInfo("UKETSUKE_NO", JCMBUtilityCommon.objToStr(infoMap.get("受付番号")));

		if (mailDiv.equals("1")) {
			// 電話番号
			sendMail.setSendMailInfo("TEL_NO", JCMBUtilityCommon.objToStr(infoMap.get("電話番号")));
		} else {
			// 電話番号
			sendMail.setSendMailInfo("TEL_NO", JCMBUtilityCommon.objToStr(infoMap.get("携帯電話番号")));
		}

		// 携帯向けの設定
		if ("2".equals(mailDiv)) {
			// 興行コード
			sendMail.setSendMailInfo("SHOW_CODE", JCMBUtilityCommon.objToStr(mapTikanMonziKeitai.get("興行コード")));
			// 興行サブコード
			sendMail.setSendMailInfo("SHOW_SUBCODE", JCMBUtilityCommon.objToStr(mapTikanMonziKeitai.get("興行サブコード")));
			// 問合せ受付番号
			sendMail.setSendMailInfo("REC_NO", JCMBUtilityCommon.objToStr(mapTikanMonziKeitai.get("問合せ受付番号")));
			// 業務コード
			sendMail.setSendMailInfo("BUSINESS_CD", JCMBUtilityCommon.objToStr(mapTikanMonziKeitai.get("業務コード")));
			// 受付情報区分
			sendMail.setSendMailInfo("ACC_INFO_DIV", JCMBUtilityCommon.objToStr(mapTikanMonziKeitai.get("受付情報区分")));

			// ＰＣ向けの設定
		} else {
			// 興行コード
			sendMail.setSendMailInfo("SHOW_CODE", JCMBUtilityCommon.objToStr(mapTikanMonziPc.get("興行コード")));
			// 興行サブコード
			sendMail.setSendMailInfo("SHOW_SUBCODE", JCMBUtilityCommon.objToStr(mapTikanMonziPc.get("興行サブコード")));
			// 問合せ受付番号
			sendMail.setSendMailInfo("REC_NO", JCMBUtilityCommon.objToStr(mapTikanMonziPc.get("問合せ受付番号")));
			// 業務コード
			sendMail.setSendMailInfo("BUSINESS_CD", JCMBUtilityCommon.objToStr(mapTikanMonziPc.get("業務コード")));
			// 受付情報区分
			sendMail.setSendMailInfo("ACC_INFO_DIV", JCMBUtilityCommon.objToStr(mapTikanMonziPc.get("受付情報区分")));
		}

		// メールタイトルの設定
		sendMail.setDmailInfo("SUBJECT", mailSubject);

		// 共通メール送信部品の【setSkinChangeMap】メソッドを呼び出し
		sendMail.setSkinChangeMap(mapSkinInfo);

		String tmpNo = getTemplateInfo(mailType).getTmpNo();
		if ("2".equals(mailDiv)) {
			// 送信先が携帯に向けの時、テンプレート番号の１桁目に"1"を付けする。
			tmpNo = "1" + JCMBUtilityCommon.getSubString(tmpNo, 1);

			sendMail.send(tmpNo, this.mapSasikomiMonkuKeitai, mapTikanMonziKeitai, language_code);
		} else {
			sendMail.send(tmpNo, mapSasikomiMonku, mapTikanMonziPc, language_code);
		}
		JCMLog.debugLog(JCMBConstants.DEBUG_LOG_LEVEL, "◆ Webメール  end");

	}

	/**
	 * 処理テンプレートファイル情報を取得する。
	 *
	 * @param mailType
	 *            メール種別
	 * @param tmplInfo
	 * 				テンプレート情報クラス
	 * @throws JCFException
	 *             続行不可能な例外が発生した場合
	 */
	private void getSyoriTmpInfo(String pMailType, TemplateInfo tmplInfo) throws JCFException {
		JCMLog.debugLog(JCMBConstants.DEBUG_LOG_LEVEL, " 【メールタイプ】 " + pMailType);

		// メールロジックファイルの区分け語
		String TEMPLATE_NO = "[TEMPLATE_NO]"; // ﾃﾝﾌﾟﾚｰﾄﾌｧｲﾙNo
		String REPIACE_FILE = "[REPLACE_FILE]";// 差替文言ﾌｧｲﾙ名=ﾀｸﾞ名
		String INSERT_WORD = "[INSERT_WORD]"; // 置換対象文字
		String SQL_NO = "[SQL_NO]"; // ＳＱＬ番号

		// メール種別より処理テンプレートファイルを決める
		String logicFileName = JCMCommonProperties.getProperty("MAIL_TEMPLATE_PATH")
				+ "0" + pMailType + ".info";

		JCMLog.debugLog(JCMBConstants.DEBUG_LOG_LEVEL, "◆【処理テンプファイル名】" + logicFileName);

		// ■メールロジックファイル読み込み
		JCMBSendMailUtil util = new JCMBSendMailUtil();
		ArrayList aryTikanMonzi = new ArrayList();
		ArrayList arySasikomiMonku = new ArrayList();
		List list = util.readFile(logicFileName);
		// retInfo.setTmpNo("");
		// メールロジックファイルフラグ
		// (0:処理なし 1:TEMPLATE_NO　2:REPIACE_FILE　3:INSERT_WORD)
		int lineFlg = 0;
		for (int i = 0; i < list.size(); i++) {
			String tmp_str = (String) list.get(i);
			if (tmp_str.equals(TEMPLATE_NO)) {
				// 次の行から[TEMPLATE_NO]
				lineFlg = 1;
			} else if (tmp_str.equals(REPIACE_FILE)) {
				// 次の行から[REPIACE_FILE]
				lineFlg = 2;
			} else if (tmp_str.equals(INSERT_WORD)) {
				// 次の行から[INSERT_WORD]
				lineFlg = 3;
			} else if (tmp_str.equals(SQL_NO)) {
				// 次の行から[SQL_NO]
				lineFlg = 4;
			} else {
				if (lineFlg == 1) {
					// ■メールテンプレート
					tmplInfo.setTmpNo(tmp_str);
				} else if (lineFlg == 2) {
					// ■差替情報List作成
					arySasikomiMonku.add(tmp_str);
				} else if (lineFlg == 3) {
					// ■置換対象文字列の作成
					aryTikanMonzi.add(tmp_str);
				} else if (lineFlg == 4) {
					// ■SQL文字列の作成
					tmplInfo.getArySqlNo().add(tmp_str);
				}
			}
		}
		// 差込情報をHashMapに積み込む
		if (arySasikomiMonku != null && arySasikomiMonku.size() > 0) {
			for (int i = 0; i < arySasikomiMonku.size(); i++) {
				tmplInfo.getMapSasikomiMonku_Tuzyo().put(arySasikomiMonku.get(i), "");
			}
		}

		// 置換情報をHashMapに積み込む
		if (aryTikanMonzi != null && aryTikanMonzi.size() > 0) {
			for (int j = 0; j < aryTikanMonzi.size(); j++) {
				tmplInfo.getMapTikanMonzi_Tuzyo().put(aryTikanMonzi.get(j), "");
			}
		}

		tmplInfo.getMapTikanMonzi_Tuzyo().put("受付番号", "");
		tmplInfo.getMapTikanMonzi_Tuzyo().put("興行コード", "");
		tmplInfo.getMapTikanMonzi_Tuzyo().put("興行サブコード", "");
		tmplInfo.getMapTikanMonzi_Tuzyo().put("問合せ受付番号", "");
		tmplInfo.getMapTikanMonzi_Tuzyo().put("業務コード", "");
		tmplInfo.getMapTikanMonzi_Tuzyo().put("受付情報区分", "");

	}

	/**
	 * 種別番号により振り分け、区分を設定します。<br>
	 *
	 * @param editItem
	 * 				置換キー/差込文言キー内容設定クラス
	 * @param kanriNo
	 *            管理番号
	 * @param strMailsyubetsu
	 *            種別番号
	 * @param dbBean
	 * 				メール送信処理ＢＴオーダーレベル項目Beanクラス
	 * @return String 区分
	 */
	private String logicAMC(JCMBSendMailEditItem editItem, String kanriNo, String strMailsyubetsu,
			JCMBSendMailOrderBean dbBean) throws Exception {

		JCMLog.debugLog(JCMBConstants.DEBUG_LOG_LEVEL, "◇　start");
		JCMLog.debugLog(JCMBConstants.DEBUG_LOG_LEVEL, "◇syubetsuNo　start" + strMailsyubetsu);
		// 返却値の変数名
		String kubun = "";

		short syubetsuNo = Short.parseShort(strMailsyubetsu);

		// 支払方法
		// 1:カード
		// 2:オーソリNG
		// 3:振替金
		// 4:未決済
		// 5:カードコンビニ
		// 6:継続コンビニ
		// 7:コンビニ
		int intShirahaiHohokbn = editItem.checkShiharaiHouhouRap(dbBean, strMailsyubetsu);

		// 許可
		boolean flg = false;
		String strErr = "";
		// 種別番号より振り分けチェック
		switch (syubetsuNo) {
			case JCMBConstants.PRE_TOUSEN_MAIL : // 2
				// 1:カード/7:コンビニ/8:コンビニ入金済/3:振替金/5:カードコンビニ/6:継続コンビニ/4:未決済のみ許可
				if (intShirahaiHohokbn == 1 || intShirahaiHohokbn == 7 || intShirahaiHohokbn == 8
						|| intShirahaiHohokbn == 3 || intShirahaiHohokbn == 5 || intShirahaiHohokbn == 6
						|| intShirahaiHohokbn == 4) {
					flg = true;
				} else {
					strErr = " 支払方法【" + changMongon(intShirahaiHohokbn, 1) + "】";
				}
				break;
			case JCMBConstants.PRE_RAKUSEN : // 3
				// 1:カード/7:コンビニ/3:振替金/2:オーソリNG/6:継続コンビニ/4:未決済のみ許可
				if (intShirahaiHohokbn == 1 || intShirahaiHohokbn == 7 || intShirahaiHohokbn == 3
						|| intShirahaiHohokbn == 2 || intShirahaiHohokbn == 6 || intShirahaiHohokbn == 4) {
					flg = true;
				} else {
					strErr = " 支払方法【" + changMongon(intShirahaiHohokbn, 1) + "】";
				}
				break;
			case JCMBConstants.SAKIBARAI_PRE_TOUSEN_MAIL : // 8
				// 1:カード/7:コンビニ/3:振替金/5:カードコンビニ/6:継続コンビニ/4:未決済のみ許可
				if (intShirahaiHohokbn == 1 || intShirahaiHohokbn == 8 || intShirahaiHohokbn == 3
						|| intShirahaiHohokbn == 5 || intShirahaiHohokbn == 6 || intShirahaiHohokbn == 4) {
					flg = true;
				} else {
					strErr = " 支払方法【" + changMongon(intShirahaiHohokbn, 1) + "】";
				}
				break;
			case JCMBConstants.SAKIBARAI_PRE_RAKUSEN_MAIL : // 9
				// 1:カード/7:コンビニ/3:振替金/6:継続コンビニ/4:未決済のみ許可
				if (intShirahaiHohokbn == 1 || intShirahaiHohokbn == 7 || intShirahaiHohokbn == 8
						|| intShirahaiHohokbn == 3 || intShirahaiHohokbn == 6 || intShirahaiHohokbn == 4) {
					flg = true;
				} else {
					strErr = " 支払方法【" + changMongon(intShirahaiHohokbn, 1) + "】";
				}
				break;
			default :
				Exception e = new Exception("メール種別番号が業務想定外です。");
				String message = JCMMessageManager.getMessage("MB1903E");
				JCMLog.errorLog(JCMBConstants.ERROR_LOG_LEVEL, "MB1903E", message, e);
				throw new JCFException(e, "MB1903E", message);
		}
		if (flg == false) {
			Exception e = new Exception("メール種別に対するデータが不正です。◆管理番号= 【" + kanriNo + "】◆メール種別= " + syubetsuNo + strErr);
			String message = JCMMessageManager.getMessage("MB1903E") + strErr;
			JCMLog.errorLog(JCMBConstants.ERROR_LOG_LEVEL, "MB1903E", message, e);
			throw new JCFException(e, "MB1903E", message);
		}
		JCMLog.debugLog(JCMBConstants.DEBUG_LOG_LEVEL, "◆kubun =" + kubun);
		JCMLog.debugLog(JCMBConstants.DEBUG_LOG_LEVEL, "◇　end");

		return kubun;
	}

	/**
	 * エラーログ対応。文言に変更する<br />
	 *
	 * @param intKbn
	 * 				区分
	 * @param intdata
	 * 				コード
	 * @return	String 文言
	 */
	private String changMongon(int intdata, int intKbn) {
		String strRes = "";
		// 区分_１---------支払方法
		if (intKbn == 1) {
			if (intdata == 1) {
				// 1:カード
				strRes = "カード";
			} else if (intdata == 2) {
				// 2:オーソリNG
				strRes = "オーソリNG";
			} else if (intdata == 3) {
				// 3:振替金
				strRes = "振替金";
			} else if (intdata == 4) {
				// 4:未決済
				strRes = "未決済";
			} else if (intdata == 5) {
				// 5:カードコンビニ
				strRes = "カードコンビニ";
			} else if (intdata == 6) {
				// 6:継続コンビニ
				strRes = "継続コンビニ";
			} else if (intdata == 7) {
				// 7:コンビニ
				strRes = "コンビニ";
			} else if (intdata == 8) {
				// 7:コンビニ
				strRes = "コンビニ入金済";
			} else {
				strRes = "パターンなし";
			}
		}
		return strRes;
	}

	/**
	 * NULLの処理を行います。<br />
	 *
	 * @param obj
	 * @return	String
	 */
	private String objToStr(Object obj) {
		return JCMBUtilityCommon.objToStr(obj);
	}

	/**
	 * DBBeanより個人情報を取得します。<br>
	 *
	 * @param dbOrderBean
	 *            JCMBSendMailOrderBean ＢＴオーダーレベル項目Bean
	 * @return HashMap 個人情報
	 */
	private HashMap getPersonInfo(JCMBSendMailOrderBean dbOrderBean) throws Exception {
		// 返却値の変数名
		HashMap rtnMap = new HashMap();

		if (dbOrderBean == null) {
			JCMLog.debugLog(JCMBConstants.DEBUG_LOG_LEVEL, "データ情報が取れません。");
			return rtnMap;
		}

		// 会員/一見区分
		String strKaiinKbn = dbOrderBean.getOKainIkkenKbn();
		JCMLog.debugLog(JCMBConstants.DEBUG_LOG_LEVEL, " ◆会員/一見区分  → " + strKaiinKbn);
		if ("1".equals(strKaiinKbn)) {

			// 障害-23336（【新会員】抽選結果確認メールが携帯用の文面になっていた）UPD START
			// ■会員の場合
//			String strSosikiSyubetu = dbOrderBean.getOSosikiSyubetu();
//			JCMLog.debugLog(JCMBConstants.DEBUG_LOG_LEVEL," ◆組織種別  → " + strSosikiSyubetu);
//			if("1".equals(strSosikiSyubetu) || "2".equals(strSosikiSyubetu)){
//				// 　●１：ｅ＋組織、２：ｅ＋共有組織の場合
//				// 　　ＬＴｅ＋会員を参照
//				rtnMap.put("ＰＣメールアドレス＿ドメイン",dbOrderBean.getOPCMailAdsDomainKaiin()) ;
//				rtnMap.put("ＰＣメールアドレス",dbOrderBean.getOPCMailAdsKaiin());
//				rtnMap.put("ＰＣメールアドレス送信要否",dbOrderBean.getOPCMailAdsYohiKaiin());
//				rtnMap.put("携帯メールドメイン",dbOrderBean.getOKeitaiMailAdsDomainKaiin());
//				rtnMap.put("携帯メールアドレス",dbOrderBean.getOKeitaiMailAdsKaiin());
//				rtnMap.put("携帯メールアドレス送信要否",dbOrderBean.getOkeitaiMailAdsYohiKaiin());
//				rtnMap.put("組織コード",dbOrderBean.getOSosikiCd());
//				rtnMap.put("会員ＩＤ",dbOrderBean.getOKainId());
//				rtnMap.put("カナ氏名＿姓",dbOrderBean.getOKanaShimeiSeiKaiin());
//				rtnMap.put("カナ氏名＿名",dbOrderBean.getOKanaShimeiMeiKaiin());
//				rtnMap.put("漢字氏名＿姓",dbOrderBean.getOShimeiSeiKaiin());
//				rtnMap.put("漢字氏名＿名",dbOrderBean.getOShimeiMeiKaiin());
//				rtnMap.put("電話番号",dbOrderBean.getOPhonNoKaiin());
//				rtnMap.put("受付番号",dbOrderBean.getOUketukeNo());
//				rtnMap.put("携帯電話番号",dbOrderBean.getOKeitaiPhonNoKaiin());
//				rtnMap.put("ＰＣメール不達フラグ",dbOrderBean.getOMailFutatsuFlgKaiin());
//				rtnMap.put("携帯メール不達フラグ",dbOrderBean.getOKeitaiMailFutatsuFlgKaiin());

			JCMBCheckCommon chkCommon = new JCMBCheckCommon(this.g_Connection);
			if (chkCommon.isShinKaiinJoho(dbOrderBean.getOSosikiCd(), dbOrderBean.getOKainId())) {
				rtnMap = getShinKaiinInfo(dbOrderBean.getOKainId(), dbOrderBean.getOSosikiCd(), this.g_Connection);
			} else {
			// 障害-23336（【新会員】抽選結果確認メールが携帯用の文面になっていた）UPD END
				// 　　ＬＴ他組織会員を参照
				rtnMap.put("ＰＣメールアドレス＿ドメイン", dbOrderBean.getOPCMailAdsDomainTa());
				rtnMap.put("ＰＣメールアドレス", dbOrderBean.getOPCMailAdsTa());
				rtnMap.put("ＰＣメールアドレス送信要否", dbOrderBean.getOPCMailAdsYohiTa());
				rtnMap.put("携帯メールドメイン", dbOrderBean.getOKeitaiMailAdsDomainTa());
				rtnMap.put("携帯メールアドレス", dbOrderBean.getOKeitaiMailAdsTa());
				rtnMap.put("携帯メールアドレス送信要否", dbOrderBean.getOkeitaiMailAdsYohiTa());
				rtnMap.put("組織コード", dbOrderBean.getOSosikiCd());
				rtnMap.put("会員ＩＤ", dbOrderBean.getOKainId());
				rtnMap.put("カナ氏名＿姓", dbOrderBean.getOKanaShimeiSeiTa());
				rtnMap.put("カナ氏名＿名", dbOrderBean.getOKanaShimeiMeiTa());
				rtnMap.put("漢字氏名＿姓", dbOrderBean.getOShimeiSeiTa());
				rtnMap.put("漢字氏名＿名", dbOrderBean.getOShimeiMeiTa());
				rtnMap.put("電話番号", dbOrderBean.getOPhonNoTa());
				rtnMap.put("受付番号", dbOrderBean.getOUketukeNo());
				rtnMap.put("携帯電話番号", dbOrderBean.getOKeitaiPhonNoTa());
				rtnMap.put("ＰＣメール不達フラグ", dbOrderBean.getOMailFutatsuFlgTa());
				rtnMap.put("携帯メール不達フラグ", dbOrderBean.getOKeitaiMailFutatsuFlgTa());
			}
		} else {
			// ■一見の場合
			// 　ＢＴ一見購入者情報を参照
			rtnMap.put("ＰＣメールアドレス＿ドメイン", "");
			rtnMap.put("ＰＣメールアドレス", dbOrderBean.getOPCMailAdsItigen());
			rtnMap.put("ＰＣメールアドレス送信要否", "");
			rtnMap.put("携帯メールドメイン", "");
			rtnMap.put("携帯メールアドレス", dbOrderBean.getOKeitaiMailAdsItigen());
			rtnMap.put("携帯メールアドレス送信要否", "");
			rtnMap.put("組織コード", "");
			rtnMap.put("会員ＩＤ", dbOrderBean.getOUketukeId());
			rtnMap.put("カナ氏名＿姓", dbOrderBean.getOKanaShimeiSeiItigen());
			rtnMap.put("カナ氏名＿名", dbOrderBean.getOKanaShimeiMeiItigen());
			rtnMap.put("漢字氏名＿姓", dbOrderBean.getOShimeiSeiItigen());
			rtnMap.put("漢字氏名＿名", dbOrderBean.getOShimeiMeiItigen());
			rtnMap.put("電話番号", dbOrderBean.getOPhonNoItigen());
			rtnMap.put("受付番号", "");
			rtnMap.put("携帯電話番号", dbOrderBean.getOPhonNoItigen());
			rtnMap.put("ＰＣメール不達フラグ", "");
			rtnMap.put("携帯メール不達フラグ", "");
		}

		return rtnMap;
	}

	/**
	 * 障害-23336（【新会員】抽選結果確認メールが携帯用の文面になっていた）ADD
	 *
	 * 新会員情報をセット
	 *
	 * @param kaiinId 会員ID
	 * @param soshikiCd 組織コード
	 * @return 会員情報のハッシュマップ
	 */
	private HashMap getShinKaiinInfo(String kaiinId, String soshikiCd, Connection con) throws Exception {

		JCMLog.debugLog(JCMBConstants.DEBUG_LOG_LEVEL, "getShinKaiinInfo：実行開始");

		//会員情報格納用のHashMap
		HashMap map = new HashMap();

		JCMMysqlDBAccesser dbMysqlAccesser = new JCMMysqlDBAccesser(con);
		Connection co = JCMMysqlConnection.getConnection();

		// 検索条件を作成します。
		ArrayList condition = new ArrayList();

		// 組織コードを設定します。
		condition.add(soshikiCd);
		// 会員ＩＤを設定します。
		condition.add(kaiinId);

		// DBアクセス部品を呼び出し、検索を実行します。
		JCMLog.debugLog(JCMBConstants.DEBUG_LOG_LEVEL,
				"SQL発行開始。condition:" + condition.toString());
		ArrayList resultAL = dbMysqlAccesser.select("SB01059", condition, co);
		JCMLog.debugLog(JCMBConstants.DEBUG_LOG_LEVEL,
				"SQL発行終了。resultAL:" + resultAL.toString());

		if( resultAL.size() > 0 ){
			HashMap resMap = (HashMap)resultAL.get(0);

			map.put("組織コード", (String)resMap.get("SOSHIKI_CODE"));
			map.put("会員ＩＤ", (String)resMap.get("MEMBER_ID"));
			map.put("受付番号",(String)resMap.get("UKETSUKE_NUMBER"));
			map.put("漢字氏名＿姓", (String)resMap.get("SHIMEI_SEI"));
			map.put("漢字氏名＿名", (String)resMap.get("SHIMEI_MEI"));
			map.put("カナ氏名＿姓", (String)resMap.get("SHIMEI_KANA_SEI"));
			map.put("カナ氏名＿名", (String)resMap.get("SHIMEI_KANA_MEI"));
			map.put("電話番号", (String)resMap.get("TELNUM"));
			map.put("携帯電話番号", (String)resMap.get("MOBILE_TELNUM"));
			map.put("メールアドレス1", (String)resMap.get("PC_MAIL_ADDRESS"));
			map.put("メールアドレス1アカウント", (String)resMap.get("PC_MAIL_ACCOUNT"));
			map.put("メールアドレス1ドメイン", (String)resMap.get("PC_MAIL_DOMAIN"));
			map.put("メールアドレス1不達フラグ", (String)resMap.get("PC_FUTATSU_FLAG"));

			if (!mobileDomain.contains((String)resMap.get("PC_MAIL_DOMAIN"))){
				map.put("メールアドレス1キャリア種別", "1");
			}else{
				map.put("メールアドレス1キャリア種別", "2");
			}

			map.put("メールアドレス2", (String)resMap.get("MB_MAIL_ADDRESS"));
			map.put("メールアドレス2アカウント", (String)resMap.get("MB_MAIL_ACCOUNT"));
			map.put("メールアドレス2ドメイン", (String)resMap.get("MB_MAIL_DOMAIN"));
			map.put("メールアドレス2不達フラグ", (String)resMap.get("MB_FUTATSU_FLAG"));

			if (!mobileDomain.contains((String)resMap.get("MB_MAIL_DOMAIN"))){
				map.put("メールアドレス2キャリア種別", "1");
			}else{
				map.put("メールアドレス2キャリア種別", "2");
			}
		}

		JCMLog.debugLog(JCMBConstants.DEBUG_LOG_LEVEL, "getShinKaiinInfo：実行終了");

		return map;
	}

	/**
	 * 障害-23336（【新会員】抽選結果確認メールが携帯用の文面になっていた）ADD
	 *
	 * 「ＺＭプロパティ」から携帯のドメインを取得し、ArrayListに格納する
	 * @return 携帯のドメインリスト
	 */
	private ArrayList getMobileDomain() {
		JCMLog.debugLog(JCMBConstants.DEBUG_LOG_LEVEL, "getMobileDomain：実行開始");

		ArrayList domainList = new ArrayList();
		String[] mobileDomainKey = {"DOCOMO_DOMAIN", "EZWEB_DOMAIN", "SOFTBANK_DOMAIN"};

		for (int cnt = 0; cnt < mobileDomainKey.length; cnt++) {
			String value = JCMCommonProperties.getProperty(mobileDomainKey[cnt]);
			String[] valueList = value.split(";");

			JCMLog.debugLog(JCMBConstants.DEBUG_LOG_LEVEL, "MobileDomain（" + mobileDomainKey[cnt] + "）＝" + value);

			for(int i = 0; i < valueList.length; i++){
				domainList.add(valueList[i]);
			}
		}

		JCMLog.debugLog(JCMBConstants.DEBUG_LOG_LEVEL, "getMobileDomain：実行終了");

		return domainList;
	}

	/**
	 * 文字列の範囲チェックを行う<br />
	 *
	 *
	 * @param inStr
	 *            チェックする文字列
	 * @param intFrom
	 *            最小値
	 * @param intTo
	 *            最大値
	 * @return true:範囲内、false:範囲外(or数字ではない)
	 */
	private boolean checkNumber(String inStr, int intFrom, int intTo) {
		try {
			if (!JCMExtUtil.isNumeric(inStr)){
				return false;
			}

			int intTajyuNo = Integer.parseInt(inStr);
			if (intFrom > intTo || intTajyuNo < intFrom || intTajyuNo > intTo) {
				return false;
			}
			return true;
		} catch (Exception e) {
			return false;
		}

	}

	/**
	 * メール送信処理異常発生の時処理です。<br>
	 * <br>
	 *
	 * @param strMailType
	 *            String メール種別
	 * @param strMailTiTle
	 *            String メールタイトル
	 * @param strError
	 *            String 異常内容 <br>
	 * @return なし
	 */
	private void errControl(String strMailType, String strMailTiTle, String strError) {
		// DebugLog部分
		JCMLog.debugLog(JCMBConstants.DEBUG_LOG_LEVEL, "▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼ <BR>");
		JCMLog.debugLog(JCMBConstants.DEBUG_LOG_LEVEL, "【" + strMailType + "】番『" + strMailTiTle + "』処理時、異常が発生しました。<BR>");
		JCMLog.debugLog(JCMBConstants.DEBUG_LOG_LEVEL, "異常内容：<BR>");
		JCMLog.debugLog(JCMBConstants.DEBUG_LOG_LEVEL, strError + "<BR>");
		JCMLog.debugLog(JCMBConstants.DEBUG_LOG_LEVEL, "▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲");

		// errorLog部分
		JCMLog.errorLog(JCMBConstants.ERROR_LOG_LEVEL, "MB1901E", "▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼ <BR>", null);
		JCMLog.errorLog(JCMBConstants.ERROR_LOG_LEVEL, "MB1901E", "【" + strMailType + "】番『" + strMailTiTle
				+ "』メール処理時、異常が発生しました。<BR>", null);
		JCMLog.errorLog(JCMBConstants.ERROR_LOG_LEVEL, "MB1901E", "異常内容：<BR>", null);
		JCMLog.errorLog(JCMBConstants.ERROR_LOG_LEVEL, "MB1901E", strError + "<BR>", null);
		JCMLog.errorLog(JCMBConstants.ERROR_LOG_LEVEL, "MB1901E", "▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲", null);

	}

	/**
	 * コミットを行います<br>
	 *
	 * @throws JBFBusinessException
	 *             バッチ業務アプリで致命的エラー(異常)を検知した場合
	 */
	private void commitTransaction() throws JBFBusinessException {

		try {
			g_Connection.commit();
		} catch (Exception e) {
			String msgID = "MAA245W";
			String msgStr = JCMMessageManager.getMessage(msgID, "ＤＢ", "commit");
			JCMLog.errorLog(JCMBConstants.ERROR_LOG_LEVEL, msgID, msgStr, e);
			throw new JBFBusinessException(msgStr, e);
		}
	}

	/**
	 * テンプレート情報を取得<br />
	 *
	 *
	 * @param pMailType
	 * @return
	 * @throws JCFException
	 * @throws Exception
	 */
	private TemplateInfo getTemplateInfo(String pMailType) throws JCFException, Exception {
		TemplateInfo retInfo = null;

		if (templateInfoMap != null && templateInfoMap.containsKey(pMailType)) {
			retInfo = (TemplateInfo) templateInfoMap.get(pMailType);
		} else {
			retInfo = new TemplateInfo();
			getSyoriTmpInfo(pMailType, retInfo);
			if (templateInfoMap == null) {
				templateInfoMap = new HashMap();
			}
			templateInfoMap.put(pMailType, retInfo);
		}

		return retInfo;
	}

	/**
	 * テンプレート情報<br />
	 * テンプレート情報を保存します。<br />
	 *
	 */
	private class TemplateInfo {
		// テンプレートコード
		private String tmpNo = "";
		// 検索用SQL一覧
		private ArrayList arySqlNo = new ArrayList();
		// メールタイプ単位より差込文句用HashMap(通常（特約以外）)
		private HashMap mapSasikomiMonku_Tuzyo = new HashMap();
		// メールタイプ単位より置換文字用HashMap(通常（特約以外）)
		private HashMap mapTikanMonzi_Tuzyo = new HashMap();

		/**
		 * @return tmpNo
		 */
		public String getTmpNo() {
			return tmpNo;
		}

		/**
		 * @param tmpNo
		 *            セットする tmpNo
		 */
		public void setTmpNo(String tmpNo) {
			this.tmpNo = tmpNo;
		}

		/**
		 * @return arySqlNo
		 */
		public ArrayList getArySqlNo() {
			return arySqlNo;
		}

		/**
		 * @return mapSasikomiMonku_Tuzyo
		 */
		public HashMap getMapSasikomiMonku_Tuzyo() {
			return mapSasikomiMonku_Tuzyo;
		}

		/**
		 * @return mapTikanMonzi_Tuzyo
		 */
		public HashMap getMapTikanMonzi_Tuzyo() {
			return mapTikanMonzi_Tuzyo;
		}

	}

}
