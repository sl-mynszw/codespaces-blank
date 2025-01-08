
package L.apl.web;

import java.io.FileInputStream;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.StringTokenizer;

import com.fujitsu.uji.compo.ComboBox;
import com.fujitsu.uji.compo.FieldString;
import com.fujitsu.uji.compo.FieldTextArea;
import com.fujitsu.uji.compo.ListBox;
import com.fujitsu.uji.model.list.DefaultListModel;
import com.fujitsu.uji.util.MimeSource;

import B.gc.common.JCMBUtilityCommon;
import B.gc.web.JOABConstant;
import L.apl.web.bean.JOALMailTemplate;
import L.apl.web.common.JOALConstant;
import L.gc.common.JCMLMCommonDB;
import L.gc.common.JCMLQConstants;
import L.gc.web.JCMLMWebCommonDB;
import L.gc.web.JCMLQToiawaseRegistry;
import W.gc.common.JCMAppellationInfo;
import W.gc.common.JCMAppellationUtility;
import W.gc.common.JCMClock;
import W.gc.common.api.JCMWSinkaiinAccesser;
import W.gc.common.api.shinkaiin.JCMWMemberSearchApi;
import W.gc.common.api.shinkaiin.bean.JCMWCustomerBean;
import W.gc.common.api.shinkaiin.bean.JCMWShinKaiinApiArrayResultBean;
import W.gc.common.api.shinkaiin.bean.JCMWShinKaiinApiIoBean;
import Z.fw.common.JCFDBUtil;
import Z.fw.web.JOFDBAccesser;
import Z.fw.web.JOFFrameworkRuntimeException;
import Z.fw.web.JOFGyomuException;
import Z.fw.web.JOFGyomuRuntimeException;
import Z.fw.web.JOFUtil;
import Z.gc.common.Language;
import Z.util.common.JCMCheckUtil;
import Z.util.common.JCMCommonProperties;
import Z.util.common.JCMInternalSendMail;
import Z.util.common.JCMLog;
import Z.util.common.JCMMessageManager;
import Z.util.web.JCMOnlineBatchStart;

/**
 * アウトバウンド詳細・登録 ロジッククラスです。<br>
 * 業務ではビジネスロジックの呼び出し、出力データの生成、
 * <pre>
 * $History:: JOAL2201BL.java                                $
 * アウトバウンド詳細・登録
 * </pre>
 *
 * @version $Revision:: 15                                  $
 * @author  $Author:: Ok_kim                                 $
 * @see  JOAL2201BL
 */
public class JOAL2201BL {

	private final String CLASS_NAME = "JOAL2201BL";

	//定数を定義する
	private final static String KAIIN_KBN_KAIIN			= "1";		// 会員一見区分：会員
	private final static String KAIIN_KBN_ICHIGEN		= "2";		// 会員一見区分：一見

	private final static String SOSHIKI_KIND_EPLUS		= "1";		// 組織種別：ｅ＋組織
	private final static String SOSHIKI_KIND_ECOMMON		= "2";		// 組織種別：ｅ＋共有組織
	private final static String SOSHIKI_KIND_TEIKEIASP	= "3";		// 組織種別：他組織提携ＡＳＰ組織
	private final static String SOSHIKI_KIND_KANZENASP	= "4";		// 組織種別：他組織完全ＡＳＰ組織

	private final static String PCKEITAIKBN_PC			= "0";		// ＰＣ携帯区分：ＰＣ
	private final static String PCKEITAIKBN_KEITAI		= "1";		// ＰＣ携帯区分：携帯
	private final static String PCKEITAIKBN_SONOTA		= "2";		// ＰＣ携帯区分：その他

	private final static int IDX_UKETSUKE_INFO_KUBUN = 10;

	public static String pcFlag							= "0";
	public static String kaitaiFlag						= "1";
	public static String telFlag							= "2";
	private String pc										= "0";
	private String keitai									= "1";
	private String g_TyusyutuAru							= "1";
	private String g_TyusyutuNasi							= "0";

	final String PROGRAM_ID = "L2201";

	/** 改行コード */
	private String RETURN_CODE = "\r\n";

	/**
	 * 興行サブ情報を取得する処理を行います。
	 * @param 	inGyoukouCode 		興行コード
	 * @param 	inSubCode 			興行コードサブ
	 * @return String[][]			受付情報
	 * @throws JOFGyomuException	業務アプリで致命的エラー(異常)を検知した場合
	 * @throws SQLException		DBアクセス処理で致命的エラー(異常)を検知した場合
	 */
	public String[][] getKougyoSubInfo(String strKougyoCD) throws JOFGyomuException, SQLException {

		//DBアクセス
		JOFDBAccesser dbAccesserSLQ4462 = new JOFDBAccesser();
		ArrayList conSLQ4462 = new ArrayList();
		ResultSet rsSLQ4462 = null;
		JCMCheckUtil checkUtil = new JCMCheckUtil();

		ArrayList lstKougyoCode = new ArrayList();
		ArrayList lstKougyoSubCode = new ArrayList();
		ArrayList lstKougyoName = new ArrayList();
		ArrayList lstKaijyoName = new ArrayList();

		String[][] retKougyoSubJoho = null;
		String strKougyoCode	= "";
		String strKougyoSubCode	= "";
		String strKougyoName	= "";
		String strKaijyoName	= "";
		int intRSCount = 0;

		conSLQ4462.add(strKougyoCD);

		try {
			rsSLQ4462 = dbAccesserSLQ4462.getResultSetBySelect("SLQ4462", conSLQ4462);

			while (rsSLQ4462.next()) {
				//興行コード
				if (checkUtil.isCheckExist(rsSLQ4462.getString(1))) {
					strKougyoCode = rsSLQ4462.getString(1);
					JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "☆興行コード：" + strKougyoCode);
				}
				lstKougyoCode.add(intRSCount, strKougyoCode);

				//興行サブコード
				if (checkUtil.isCheckExist(rsSLQ4462.getString(2))) {
					strKougyoSubCode = rsSLQ4462.getString(2);
					JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "☆興行サブコード：" + strKougyoSubCode);
				}
				lstKougyoSubCode.add(intRSCount, strKougyoSubCode);

				//興行名称１
				if (checkUtil.isCheckExist(rsSLQ4462.getString(3))) {
					strKougyoName = rsSLQ4462.getString(3);
					JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "☆興行名称１：" + strKougyoName);
				}
				lstKougyoName.add(intRSCount, strKougyoName);

				//会場名称
				if (checkUtil.isCheckExist(rsSLQ4462.getString(4))) {
					strKaijyoName = rsSLQ4462.getString(4);
					JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "☆会場名称：" + strKaijyoName);
				}
				lstKaijyoName.add(intRSCount, strKaijyoName);

				intRSCount++;
			}

			if (intRSCount > 0) { //取得件数は1の以上場合
				retKougyoSubJoho = new String[lstKougyoCode.size()][4];
				JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "☆興行サブ情報サイズ：" + lstKougyoCode.size());
				for (int intRetCount = 0; intRetCount < lstKougyoCode.size(); intRetCount++) {
					retKougyoSubJoho[intRetCount][0] = (String) lstKougyoCode.get(intRetCount);
					retKougyoSubJoho[intRetCount][1] = (String) lstKougyoSubCode.get(intRetCount);
					retKougyoSubJoho[intRetCount][2] = (String) lstKougyoName.get(intRetCount);
					retKougyoSubJoho[intRetCount][3] = (String) lstKaijyoName.get(intRetCount);
				}
			}
		} catch (JOFGyomuException e) {
			JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "ＳＱＬ実行時エラー：" + e);
			List msgList = new ArrayList();
			msgList.add("ＳＱＬ実行時エラー");
			throw new JOFGyomuException("ML1008E", msgList);
		} finally {
			dbAccesserSLQ4462.close();
		}
		return retKougyoSubJoho;
	}

	/**
	 * 受付情報を取得する処理を行います。
	 * @param 	strKougyoCD 		興行コード
	 * @param 	strKougyoSubCD 			興行コードサブ(カンマ区切り)
	 * @return String[][]			受付情報
	 * @throws JOFGyomuException	業務アプリで致命的エラー(異常)を検知した場合
	 * @throws SQLException		DBアクセス処理で致命的エラー(異常)を検知した場合
	 */
	public String[][] doKSubSearch(String strKougyoCD, String strKougyoSubCD) throws JOFGyomuException, SQLException {

		//DBアクセス
		JCMCheckUtil	checkUtil			= new JCMCheckUtil();
		JOFDBAccesser	dbAccesserSLQ4463	= new JOFDBAccesser();
		ResultSet		rsSLQ4463			= null;

		ArrayList lstKougyoCode				= new ArrayList();
		ArrayList lstKougyoSubCode			= new ArrayList();
		ArrayList lstUketsukeInfoCode		= new ArrayList();
		ArrayList lstKouenCode				= new ArrayList();
		ArrayList lstUketsukeKubunName		= new ArrayList();
		ArrayList lstUketsukeName			= new ArrayList();
		ArrayList lstKouenbi				= new ArrayList();
		ArrayList lstKaienTime				= new ArrayList();
		ArrayList lstKaijyoCode				= new ArrayList();
		ArrayList lstKaijyoLayoutCode		= new ArrayList();
		ArrayList lstUketsukeInfoKubun		= new ArrayList();

		String strKougyoCode				= "";	//興行コード
		String strKougyoSubCode				= "";	//興行サブコード
		String strUketsukeInfoCode			= "";	//受付情報コード
		String strKouenCode					= "";	//公演コード
		String strUketsukeKubunName			= "";	//受付情報区分名称
		String strUketsukeName				= "";	//受付名称ＢＡＣＫ用
		String strKouenbi					= "";	//公演日
		String strKaienTime					= "";	//開演時間
		String strKaijyoCode				= "";	//会場コード
		String strKaijyoLayoutCode			= "";	//会場レイアウトコード
		String strUketsukeInfoKubun			= "";	//受付情報区分
		String[][] retUketsukeJoho 			= null;
		int intRSCount = 0;

		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "☆SLQ4463パラ１："+ strKougyoCD);
		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "☆SLQ4463パラ２："+ strKougyoSubCD);

		StringBuffer addSql = new StringBuffer();

		addSql.append("WHERE A.\"興行コード\" = ");
		addSql.append("'");
		addSql.append(strKougyoCD);
		addSql.append("'");
		addSql.append("AND A.\"興行サブコード\" IN (");

		String[] subCD = strKougyoSubCD.split(",");
		for (int i=0; i<subCD.length; i++) {

			if(i == 0){
				addSql.append("'");
				addSql.append(subCD[i]);
				addSql.append("'");
			}else{
				addSql.append(",'");
				addSql.append(subCD[i]);
				addSql.append("'");
			}
		}

		addSql.append(")");
		addSql.append("AND A.\"論理削除フラグ\" = '0' ");
		addSql.append("AND B.\"興行コード\" = A.\"興行コード\" ");
		addSql.append("AND B.\"興行サブコード\" = A.\"興行サブコード\" ");
		addSql.append("AND B.\"受付情報コード\" = A.\"受付情報コード\" ");
		addSql.append("AND B.\"論理削除フラグ\" = '0' ");
		addSql.append("AND C.\"興行コード\" = A.\"興行コード\" ");
		addSql.append("AND C.\"興行サブコード\" = A.\"興行サブコード\" ");
		addSql.append("AND C.\"公演コード\" = A.\"公演コード\" ");
		addSql.append("AND C.\"論理削除フラグ\" = '0' ");
		addSql.append("ORDER BY A.\"興行サブコード\", A.\"受付情報コード\", A.\"公演コード\" ");

		try {
			rsSLQ4463 = dbAccesserSLQ4463.getResultSetBySelect("SLQ4463", new ArrayList(), addSql.toString());

			while (rsSLQ4463.next()) {
				//興行コード
				if (checkUtil.isCheckExist(rsSLQ4463.getString(1))) {
					strKougyoCode = rsSLQ4463.getString(1);
					JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "☆興行コード：" + strKougyoCode);
				}
				lstKougyoCode.add(intRSCount, strKougyoCode);

				//興行サブコード
				if (checkUtil.isCheckExist(rsSLQ4463.getString(2))) {
					strKougyoSubCode = rsSLQ4463.getString(2);
					JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "☆興行サブコード：" + strKougyoSubCode);
				}
				lstKougyoSubCode.add(intRSCount, strKougyoSubCode);

				//受付情報コード
				if (checkUtil.isCheckExist(rsSLQ4463.getString(3))) {
					strUketsukeInfoCode = rsSLQ4463.getString(3);
					JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "☆受付情報コード：" + strUketsukeInfoCode);
				}
				lstUketsukeInfoCode.add(intRSCount, strUketsukeInfoCode);

				//公演コード
				if (checkUtil.isCheckExist(rsSLQ4463.getString(4))) {
					strKouenCode = rsSLQ4463.getString(4);
					JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "☆公演コード：" + strKouenCode);
				}
				lstKouenCode.add(intRSCount, strKouenCode);

				//受付情報区分名称
				if (checkUtil.isCheckExist(rsSLQ4463.getString(5))) {
					strUketsukeKubunName = rsSLQ4463.getString(5);
					JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "☆受付情報区分名称：" + strUketsukeKubunName);
				}
				lstUketsukeKubunName.add(intRSCount, strUketsukeKubunName);

				//受付名称ＢＡＣＫ用
				if (checkUtil.isCheckExist(rsSLQ4463.getString(6))) {
					strUketsukeName = rsSLQ4463.getString(6);
					JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "☆受付名称ＢＡＣＫ用：" + strUketsukeName);
				}
				lstUketsukeName.add(intRSCount, strUketsukeName);

				//公演日
				if (checkUtil.isCheckExist(rsSLQ4463.getString(7))) {
					strKouenbi = rsSLQ4463.getString(7);
					JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "☆公演日：" + strKouenbi);
				}
				lstKouenbi.add(intRSCount, strKouenbi);

				//開演時間
				if (checkUtil.isCheckExist(rsSLQ4463.getString(8))) {
					strKaienTime = rsSLQ4463.getString(8);
					JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "☆開演時間：" + strKaienTime);
				}
				lstKaienTime.add(intRSCount, strKaienTime);

				//会場コード
				if (checkUtil.isCheckExist(rsSLQ4463.getString(9))) {
					strKaijyoCode = rsSLQ4463.getString(9);
					JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "☆会場コード：" + strKaijyoCode);
				}
				lstKaijyoCode.add(intRSCount, strKaijyoCode);

				//会場レイアウトコード
				if (checkUtil.isCheckExist(rsSLQ4463.getString(10))) {
					strKaijyoLayoutCode = rsSLQ4463.getString(10);
					JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "☆会場レイアウトコード：" + strKaijyoLayoutCode);
				}
				lstKaijyoLayoutCode.add(intRSCount, strKaijyoLayoutCode);

				//JCB対応 start
				//受付情報区分
				if (checkUtil.isCheckExist(rsSLQ4463.getString(11))) {
					strUketsukeInfoKubun = rsSLQ4463.getString(11);
					JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "☆受付情報区分：" + strUketsukeInfoKubun);
				}
				lstUketsukeInfoKubun.add(intRSCount, strUketsukeInfoKubun);
				//JCB対応 end

				intRSCount++;
			}

			if (intRSCount > 0) { //取得件数は1の以上場合
				retUketsukeJoho = new String[lstKougyoCode.size()][11];
				JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "☆興行サブ情報サイズ：" + lstKougyoCode.size());
				for (int icnt = 0; icnt < lstKougyoCode.size(); icnt++) {
					retUketsukeJoho[icnt][0] = (String) lstKougyoCode.get(icnt);			//興行コード
					retUketsukeJoho[icnt][1] = (String) lstKougyoSubCode.get(icnt);			//興行サブコード
					retUketsukeJoho[icnt][2] = (String) lstUketsukeInfoCode.get(icnt);		//受付情報コード
					retUketsukeJoho[icnt][3] = (String) lstKouenCode.get(icnt);				//公演コード
					retUketsukeJoho[icnt][4] = (String) lstUketsukeKubunName.get(icnt);		//受付情報区分名称
					retUketsukeJoho[icnt][5] = (String) lstUketsukeName.get(icnt);			//受付名称ＢＡＣＫ用
					retUketsukeJoho[icnt][6] = (String) lstKouenbi.get(icnt);				//公演日
					retUketsukeJoho[icnt][7] = (String) lstKaienTime.get(icnt);				//開演時間
					retUketsukeJoho[icnt][8] = (String) lstKaijyoCode.get(icnt);			//会場コード
					retUketsukeJoho[icnt][9] = (String) lstKaijyoLayoutCode.get(icnt);		//会場レイアウトコード
					retUketsukeJoho[icnt][IDX_UKETSUKE_INFO_KUBUN] = (String) lstUketsukeInfoKubun.get(icnt);	//受付情報区分
				}
			}
		} catch (JOFGyomuException e) {
			JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "ＳＱＬ実行時エラー：" + e);
			List msgList = new ArrayList();
			msgList.add("ＳＱＬ実行時エラー");
			throw new JOFGyomuException("ML1008E", msgList);
		} finally {
			dbAccesserSLQ4463.close();
		}
		return retUketsukeJoho;
	}

	/**
	 * 席種一覧を取得する処理を行います。
	 * @param strKougyoCD 興行コード
	 * @param strKougyoSubCD 興行コードサブ(カンマ区切り)
	 * @param strUketsukeCD 受付情報コード(カンマ区切り)
	 * @param strKouenCD 公演コード(カンマ区切り)
	 * @return String[][]			席種情報
	 * @throws JOFGyomuException	業務アプリで致命的エラー(異常)を検知した場合
	 * @throws SQLException		DBアクセス処理で致命的エラー(異常)を検知した場合
	 */
	public String[][] getSekisyu(String strKougyoCD, String strKougyoSubCD,
									String strUketsukeCD, String strKouenCD) throws JOFGyomuException, SQLException {

		//DBアクセス
		JCMCheckUtil	checkUtil			= new JCMCheckUtil();
		JOFDBAccesser	dbAccesserSLQ4464	= new JOFDBAccesser();
		ResultSet		rsSLQ4464			= null;

		ArrayList lstSekisyuCode			= new ArrayList();
		ArrayList lstSekisyuName			= new ArrayList();

		String strSekisyuCode				= "";	//席種コード
		String strSekisyuName				= "";	//席種名称

		String[][] retSekisyu	 			= null;
		int intRSCount = 0;

		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "☆SLQ4464パラ１："+ strKougyoCD);
		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "☆SLQ4464パラ２："+ strKougyoSubCD);
		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "☆SLQ4464パラ３："+ strUketsukeCD);
		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "☆SLQ4464パラ４："+ strKouenCD);

		StringBuffer addSql = new StringBuffer();

		addSql.append("( SELECT DISTINCT \"席種コード\" ");
		addSql.append("FROM \"ＡＴ受付公演単位席種価格\" ");
		addSql.append("WHERE \"興行コード\" = ");
		addSql.append("'");
		addSql.append(strKougyoCD);
		addSql.append("' ");
		addSql.append("AND (\"興行サブコード\", \"受付情報コード\", \"公演コード\") IN (");

		String[] subCD = strKougyoSubCD.split(",");
		String[] ukeCD = strUketsukeCD.split(",");
		String[] kouCD = strKouenCD.split(",");
		for (int i=0; i<subCD.length; i++) {
			if(i == 0){
				addSql.append("('");
				addSql.append(subCD[i]);
				addSql.append("','");
				addSql.append(ukeCD[i]);
				addSql.append("','");
				addSql.append(kouCD[i]);
				addSql.append("')");
			}else{
				addSql.append(",('");
				addSql.append(subCD[i]);
				addSql.append("','");
				addSql.append(ukeCD[i]);
				addSql.append("','");
				addSql.append(kouCD[i]);
				addSql.append("')");
			}
		}
		addSql.append(") ");
		addSql.append("AND \"論理削除フラグ\" = '0') A,");
		addSql.append("\"ＷＭ席種マスタ\" B ");
		addSql.append("WHERE B.\"席種コード\" = A.\"席種コード\" AND B.\"論理削除フラグ\" = '0' ");
		addSql.append("ORDER BY A.\"席種コード\" ");

		try {
			rsSLQ4464 = dbAccesserSLQ4464.getResultSetBySelect("SLQ4464", new ArrayList(), addSql.toString());

			while (rsSLQ4464.next()) {
				//席属コード
				if (checkUtil.isCheckExist(rsSLQ4464.getString(1))) {
					strSekisyuCode = rsSLQ4464.getString(1);
					JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "☆席属コード：" + strSekisyuCode);
				}
				lstSekisyuCode.add(intRSCount, strSekisyuCode);

				//席属名称
				if (checkUtil.isCheckExist(rsSLQ4464.getString(2))) {
					strSekisyuName = rsSLQ4464.getString(2);
					JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "☆席属名称：" + strSekisyuName);
				}
				lstSekisyuName.add(intRSCount, strSekisyuName);

				intRSCount++;
			}

			if (intRSCount > 0) {
				retSekisyu = new String[lstSekisyuCode.size()][2];
				JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "☆席種情報サイズ：" + lstSekisyuCode.size());
				for (int icnt = 0; icnt < lstSekisyuCode.size(); icnt++) {
					retSekisyu[icnt][0] = (String) lstSekisyuCode.get(icnt);			//席属コード
					retSekisyu[icnt][1] = (String) lstSekisyuName.get(icnt);			//席属名称
				}
			}
		} catch (JOFGyomuException e) {
			JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "ＳＱＬ実行時エラー：" + e);
			List msgList = new ArrayList();
			msgList.add("ＳＱＬ実行時エラー");
			throw new JOFGyomuException("ML1008E", msgList);
		} finally {
			dbAccesserSLQ4464.close();
		}
		return retSekisyu;
	}

	/**
	 * 席属一覧を取得する処理を行います。
	 * @param strKaijyoCode 会場コード
	 * @param strKaijyoLayOutCode 会場レイアウトコード
	 * @param iSekiNo 席属番号(1-4)
	 * @return String[][]			席属情報
	 * @throws JOFGyomuException	業務アプリで致命的エラー(異常)を検知した場合
	 * @throws SQLException		DBアクセス処理で致命的エラー(異常)を検知した場合
	 */
	public String[][] getSekizoku(String strKaijyoCode,String strKaijyoLayOutCode,int iSekiNo) throws JOFGyomuException, SQLException {

		//DBアクセス
		JCMCheckUtil	checkUtil			= new JCMCheckUtil();
		JOFDBAccesser	dbAccesserSLQ4465	= new JOFDBAccesser();
		ResultSet		rsSLQ4465			= null;

		ArrayList lstSekizokuCode			= new ArrayList();
		ArrayList lstSekizokuName			= new ArrayList();

		String strSekizokuCode				= "";	//席属コード
		String strSekizokuName				= "";	//席属名称

		String[][] retSekizoku	 			= null;
		int intRSCount = 0;

		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "☆SLQ4465パラ１："+ strKaijyoCode);
		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "☆SLQ4465パラ２："+ strKaijyoLayOutCode);

		StringBuffer addSql = new StringBuffer();

		if(iSekiNo == 1){
			addSql.append("( SELECT DISTINCT \"席属コード１\" AS 席属コード ");
		}else if(iSekiNo == 2){
			addSql.append("( SELECT DISTINCT \"席属コード２\" AS 席属コード ");
		}else if(iSekiNo == 3){
			addSql.append("( SELECT DISTINCT \"席属コード３\" AS 席属コード ");
		}else{
			addSql.append("( SELECT DISTINCT \"席属コード４\" AS 席属コード ");
		}

		addSql.append("FROM \"ＷＭ会場席属パターンマスタ\" ");
		addSql.append("WHERE (\"会場コード\", \"会場レイアウトコード\") IN ( ");

		String[] KjoCD = strKaijyoCode.split(",");
		String[] LayCD = strKaijyoLayOutCode.split(",");

		for (int i=0; i<KjoCD.length; i++) {
			if(i == 0){
				addSql.append("('");
				addSql.append(KjoCD[i]);
				addSql.append("','");
				addSql.append(LayCD[i]);
				addSql.append("')");
			}else{
				addSql.append(",('");
				addSql.append(KjoCD[i]);
				addSql.append("','");
				addSql.append(LayCD[i]);
				addSql.append("')");
			}
		}
		addSql.append(") ");
		addSql.append("AND \"論理削除フラグ\" = '0') A,");
		addSql.append("\"ＷＭ席属マスタ\" B ");
		addSql.append("WHERE B.\"席属コード\" = A.\"席属コード\" AND B.\"論理削除フラグ\" = '0' ");
		addSql.append("ORDER BY A.\"席属コード\" ");

		try {
			rsSLQ4465 = dbAccesserSLQ4465.getResultSetBySelect("SLQ4465", new ArrayList(), addSql.toString());

			while (rsSLQ4465.next()) {
				//席属コード
				if (checkUtil.isCheckExist(rsSLQ4465.getString(1))) {
					strSekizokuCode = rsSLQ4465.getString(1);
					JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "☆席属コード：" + strSekizokuCode);
				}
				lstSekizokuCode.add(intRSCount, strSekizokuCode);

				//席属名称
				if (checkUtil.isCheckExist(rsSLQ4465.getString(2))) {
					strSekizokuName = rsSLQ4465.getString(2);
					JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "☆席属名称：" + strSekizokuName);
				}
				lstSekizokuName.add(intRSCount, strSekizokuName);

				intRSCount++;
			}

			if (intRSCount > 0) { //取得件数は1の以上場合
				retSekizoku = new String[lstSekizokuCode.size()][2];
				JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "☆席属情報サイズ：" + lstSekizokuCode.size());
				for (int icnt = 0; icnt < lstSekizokuCode.size(); icnt++) {
					retSekizoku[icnt][0] = (String) lstSekizokuCode.get(icnt);			//席属コード
					retSekizoku[icnt][1] = (String) lstSekizokuName.get(icnt);			//席属名称
				}
			}
		} catch (JOFGyomuException e) {
			JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "ＳＱＬ実行時エラー：" + e);
			List msgList = new ArrayList();
			msgList.add("ＳＱＬ実行時エラー");
			throw new JOFGyomuException("ML1008E", msgList);
		} finally {
			dbAccesserSLQ4465.close();
		}
		return retSekizoku;
	}

	/**
	 * 抽出データ取得処理<br>
	 * ＬＴアウトバウンド対象者詳細から、抽出結果欄に表示するサマリを取得する
	 * @param 	inAutoBoundID アウトバンド
	 * @return inJcbAutoBoundID アウトバウンドID
	 * @param 	dataBean データBeanクラス
	 * @throws JOFGyomuException	業務アプリで致命的エラー(異常)を検知した場合
	 * @throws SQLException		DBアクセス処理で致命的エラー(異常)を検知した場合
	 */
	public ArrayList getTyusyutsuList(String inAutoBoundID, String inJcbAutoBoundID, L.apl.web.JOAL220101Bean dataBean) throws JOFGyomuException, SQLException {

		ArrayList retLstNaiyou = new ArrayList();
		JCMCheckUtil checkUtil = new JCMCheckUtil();
		JOFDBAccesser dbAccesserSLQ4466 = new JOFDBAccesser();
		ArrayList conSLQ4466 = new ArrayList(1);
		ResultSet rsSLQ4466 = null;
		int iNo = 0;

		String syutokuinfo = "status";
		String[][] orderInfo = getOrderStatusInfoOrYouyinInfo(syutokuinfo);

		conSLQ4466.add(inAutoBoundID);
		conSLQ4466.add(inJcbAutoBoundID);
		try {
			rsSLQ4466 = dbAccesserSLQ4466.getResultSetBySelect("SLQ4466", conSLQ4466);
			while (rsSLQ4466.next()) {
				JOAL22010101Bean joal22010101Bean = new JOAL22010101Bean();
				iNo++;

				joal22010101Bean.setStrNO(String.valueOf(iNo));							//No
				joal22010101Bean.setStrTyusyutuKensu(rsSLQ4466.getString(6));			//抽出件数

				String orderName = "";
				for (int count = 0; count < orderInfo.length; count++) {
					if(rsSLQ4466.getString(5).equals(orderInfo[count][0])){
						orderName = orderInfo[count][1];
					}
				}
				JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "☆オーダーNo：" + rsSLQ4466.getString(5));
				JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "☆オーダー名称：" + orderName);

				if(Integer.parseInt(rsSLQ4466.getString(7)) > 0){
					joal22010101Bean.setStrOrderStatus(orderName+"(*)" );	//オーダーステータス（コンビニ発券有り）
				}else{
					joal22010101Bean.setStrOrderStatus(orderName);			//オーダーステータス（コンビニ発券無し）
				}

				joal22010101Bean.setStrKougyouCode(rsSLQ4466.getString(1));				//興行コード
				joal22010101Bean.setStrKougyoumei(rsSLQ4466.getString(8));				//興行名称１
				joal22010101Bean.setStrUketsukeKouenSec(
										rsSLQ4466.getString(2)+"-"+
										rsSLQ4466.getString(3)+"-"+
										rsSLQ4466.getString(4)+" ");					//受付公演
				joal22010101Bean.setStrUketukeJohoName(rsSLQ4466.getString(9));			//受付名称
				joal22010101Bean.setStrKouenbiSec(rsSLQ4466.getString(10));				//公演日
				joal22010101Bean.setStrKaienJikanSec(rsSLQ4466.getString(11));			//開演時間
				joal22010101Bean.setStrUketukeStatus(rsSLQ4466.getString(12));			//受付状態

				retLstNaiyou.add(joal22010101Bean);

				if(checkUtil.isCheckExist(rsSLQ4466.getString(1))){
					dataBean.getStrKougyoCode().setText(rsSLQ4466.getString(1));
				}
			}
		} finally {
			dbAccesserSLQ4466.close();
		}
		return retLstNaiyou;
	}

	/**
	 * アウトバウンド対象者詳細からオーダー情報を取得する
	 * @param 	dataBean       		データBeanクラス
	 * @return
	 * @throws JOFGyomuException	業務アプリで致命的エラー(異常)を検知した場合
	 * @throws SQLException		DBアクセス処理で致命的エラー(異常)を検知した場合
	 */
	public ArrayList getTyusyutsuInfo(L.apl.web.JOAL220101Bean dataBean) throws JOFGyomuException, SQLException {

		ArrayList retGamenData = new ArrayList();
		JCMCheckUtil checkUtil = new JCMCheckUtil();
		JOFDBAccesser dbAccesserSLQ4479 = new JOFDBAccesser();
		ArrayList conSLQ4479 = new ArrayList(1);
		ResultSet rsSLQ4479 = null;
		int iNo = 0;

		conSLQ4479.add(dataBean.getStrOutBndID().getText());
		conSLQ4479.add(dataBean.getStrJcbOutBndID().getText());

		try {
			rsSLQ4479 = dbAccesserSLQ4479.getResultSetBySelect("SLQ4479", conSLQ4479);

			while (rsSLQ4479.next()) {
				JOAL22010101Bean joal22010101Bean = new JOAL22010101Bean();

				joal22010101Bean.setStrfileKougyoCD(rsSLQ4479.getString(1));				//興行コード
				joal22010101Bean.setStrfileKougyoSubCD(rsSLQ4479.getString(2));				//興行サブコード
				joal22010101Bean.setStrfileUkeInfoCD(rsSLQ4479.getString(3));				//受付情報コード
				joal22010101Bean.setStrfileKouenCD(rsSLQ4479.getString(4));					//公演コード
				joal22010101Bean.setStrfileUkeChanel(rsSLQ4479.getString(5));				//受付チャネル
				joal22010101Bean.setStrfileOdUkeDate(rsSLQ4479.getString(6));				//オーダー受付日時
				joal22010101Bean.setStrfileOdState(rsSLQ4479.getString(7));					//オーダーステータス
				joal22010101Bean.setStrfileKibouNo(rsSLQ4479.getString(8));					//希望番号
				joal22010101Bean.setStrfileShiharai(rsSLQ4479.getString(9));				//今回支払方法
				joal22010101Bean.setStrfileUketori(rsSLQ4479.getString(10));				//今回受取方法
				joal22010101Bean.setStrfileSekisyuCD(rsSLQ4479.getString(11));				//席種コード
				joal22010101Bean.setStrfileRetsuNo(rsSLQ4479.getString(12));				//列番
				joal22010101Bean.setStrfileSekiNo(rsSLQ4479.getString(13));					//席番
				joal22010101Bean.setStrfileSekizokuCD1(rsSLQ4479.getString(14));			//席属コード１
				joal22010101Bean.setStrfileSekizokuCD2(rsSLQ4479.getString(15));			//席属コード２
				joal22010101Bean.setStrfileSekizokuCD3(rsSLQ4479.getString(16));			//席属コード３
				joal22010101Bean.setStrfileSekizokuCD4(rsSLQ4479.getString(17));			//席属コード４

				retGamenData.add(joal22010101Bean);
			}
		} finally {
			dbAccesserSLQ4479.close();
		}

		return retGamenData;
	}

	/**
	 * ＬＴアウトバウンド対象者詳細新規登録処理を行います。
	 * @param 	dataBean      	 	データBeanクラス
	 * @throws JOFGyomuException	業務アプリで致命的エラー(異常)を検知した場合
	 */
	public void insertSLQ4467(L.apl.web.JOAL22010101Bean joal22010101Bean, int iCnt) throws JOFGyomuException {

		ArrayList cond = new ArrayList(46);
		JCMLQToiawaseRegistry	RegUtil				= new JCMLQToiawaseRegistry();
		JCMCheckUtil			checkUtil			= new JCMCheckUtil();
		String strFlg	= "0";
		String pcAdd	= "";
		String ktAdd	= "";

		cond.add(joal22010101Bean.getStrfileOutBndID());		//アウトバウンドＩＤ
		cond.add(String.valueOf(iCnt));							//詳細レコード通番
		cond.add(joal22010101Bean.getStrfileKanriNo());			//管理番号
		cond.add(joal22010101Bean.getStrfileKibouNo());			//希望番号
		cond.add(joal22010101Bean.getStrfileTkKanriNo());		//チケット管理番号
		cond.add(joal22010101Bean.getStrfileKnInKubun());		//会員/一見区分
		cond.add(joal22010101Bean.getStrfileSoshikiCD());		//組織コード
		cond.add(joal22010101Bean.getStrfileKainID());			//会員ＩＤ
		cond.add(joal22010101Bean.getStrfileSimei());			//氏名(姓)
		cond.add(joal22010101Bean.getStrfileMeimei());			//氏名(名)
		cond.add(joal22010101Bean.getStrfileKanaSimei());		//カナ氏名(姓)
		cond.add(joal22010101Bean.getStrfileKanaMeimei());		//カナ氏名(名)

		//メールアドレスドメイン判定
		if(checkUtil.isCheckExist(joal22010101Bean.getStrfilePcAddress())){
			strFlg = RegUtil.getPcKeitaiKbn(joal22010101Bean.getStrfilePcAddress());

			if(strFlg.equals("0")){
				pcAdd = joal22010101Bean.getStrfilePcAddress();
			}else if(strFlg.equals("1")){
				ktAdd = joal22010101Bean.getStrfilePcAddress();
			}else{
				pcAdd	= "";
				ktAdd	= "";
			}
		}
		if(checkUtil.isCheckExist(joal22010101Bean.getStrfileKtAddress())){
			strFlg = RegUtil.getPcKeitaiKbn(joal22010101Bean.getStrfileKtAddress());

			if(strFlg.equals("0")){
				pcAdd = joal22010101Bean.getStrfileKtAddress();
			}else if(strFlg.equals("1")){
				ktAdd = joal22010101Bean.getStrfileKtAddress();
			}else{
				pcAdd	= "";
				ktAdd	= "";
			}
		}
		cond.add(pcAdd);										//ＰＣメールアドレス
		cond.add(ktAdd);										//携帯メールアドレス
		cond.add(joal22010101Bean.getStrfileTelNo());			//電話番号
		cond.add(joal22010101Bean.getStrfileOdState());			//オーダーステータス
		cond.add(joal22010101Bean.getStrfileShiharai());		//今回支払方法
		cond.add(joal22010101Bean.getStrfileUketori());			//今回受取方法
		cond.add(joal22010101Bean.getStrfileSendNo1());			//発送先住所（郵便番号1）
		cond.add(joal22010101Bean.getStrfileSendNo2());			//発送先住所（郵便番号2）
		cond.add(joal22010101Bean.getStrfileTodouhukenCD());	//発送先住所（都道府県コード）
		cond.add(joal22010101Bean.getStrfileShikutyouson());	//発送先住所（市区町村）
		cond.add(joal22010101Bean.getStrfileTyoumeBanchi());	//発送先住所（丁目番地）
		cond.add(joal22010101Bean.getStrfileApartment());		//発送先住所（マンション名）
		cond.add(joal22010101Bean.getStrfileKougyoCD());		//興行コード
		cond.add(joal22010101Bean.getStrfileKougyoSubCD());		//興行サブコード
		cond.add(joal22010101Bean.getStrfileUkeInfoCD());		//受付情報コード
		cond.add(joal22010101Bean.getStrfileKouenCD());			//公演コード
		cond.add(joal22010101Bean.getStrfileZasekiAreaCD());	//座席エリアコード
		cond.add(joal22010101Bean.getStrfileSekisyuCD());		//席種コード
		cond.add(joal22010101Bean.getStrfileSekisyuKubun());	//席種区分
		cond.add(joal22010101Bean.getStrfileMoshikomiMaisu());	//申込枚数（席種計）
		cond.add(joal22010101Bean.getStrfileRetsuNo());			//列番
		cond.add(joal22010101Bean.getStrfileSekiNo());			//席番
		cond.add(joal22010101Bean.getStrfileSekizokuCD1());		//席属コード１
		cond.add(joal22010101Bean.getStrfileSekizokuCD2());		//席属コード２
		cond.add(joal22010101Bean.getStrfileSekizokuCD3());		//席属コード３
		cond.add(joal22010101Bean.getStrfileSekizokuCD4());		//席属コード４
		cond.add(JOFUtil.getRegUserID());						//登録担当者＿社員ＩＤ
		cond.add(CLASS_NAME);									//登録プログラムＩＤ
		cond.add(JOFUtil.getConsoleID());						//登録端末ＩＤ
		cond.add(JOFUtil.getRegUserID());						//更新担当者＿社員ＩＤ
		cond.add(CLASS_NAME);									//更新プログラムＩＤ
		cond.add(JOFUtil.getConsoleID());						//更新端末ＩＤ

		// ＳＱＬ発行
		JOFDBAccesser dba = new JOFDBAccesser();
		dba.execute("SLQ4467", cond);
	}

	/**
	 * csvファイル出力用データ取得処理
	 * @param 	inAutoBoundID       アウトバンド
	 * @param 	dataBean       		データBeanクラス
	 * @return ArrayList			アウトバウンドID
	 * @throws JOFGyomuException	業務アプリで致命的エラー(異常)を検知した場合
	 * @throws SQLException		DBアクセス処理で致命的エラー(異常)を検知した場合
	 */
	public ArrayList getcsvFileDataList(String inOutBndID) throws JOFGyomuException, SQLException {

		ArrayList retLstNaiyou = new ArrayList();
		JOFDBAccesser dbAccesserSLQ4468 = new JOFDBAccesser();
		ArrayList conSLQ4468 = new ArrayList(1);
		ResultSet rsSLQ4468 = null;

		conSLQ4468.add(inOutBndID);

		try {
			rsSLQ4468 = dbAccesserSLQ4468.getResultSetBySelect("SLQ4468", conSLQ4468);
			while (rsSLQ4468.next()) {

				JOAL22010101Bean joal22010101Bean = new JOAL22010101Bean();

				/** csvファイル取込用 */
				joal22010101Bean.setStrfileOutBndID(rsSLQ4468.getString(1));						//アウトバウンドID
				joal22010101Bean.setStrfileKanriNo(String.valueOf(rsSLQ4468.getInt(2)));			//管理番号
				joal22010101Bean.setStrfileKibouNo(String.valueOf(rsSLQ4468.getInt(3)));			//希望番号
				joal22010101Bean.setStrfileTkKanriNo(String.valueOf(rsSLQ4468.getInt(4)));			//チケット管理番号
				joal22010101Bean.setStrfileKnInKubun(rsSLQ4468.getString(5));						//会員／一見区分
				joal22010101Bean.setStrfileSoshikiCD(rsSLQ4468.getString(6));						//組織コード
				joal22010101Bean.setStrfileKainID(rsSLQ4468.getString(7));							//会員ID
				joal22010101Bean.setStrfileSimei(rsSLQ4468.getString(8));							//氏名（姓）
				joal22010101Bean.setStrfileMeimei(rsSLQ4468.getString(9));							//氏名（名）
				joal22010101Bean.setStrfileKanaSimei(rsSLQ4468.getString(10));						//カナ氏名（姓）
				joal22010101Bean.setStrfileKanaMeimei(rsSLQ4468.getString(11));						//カナ氏名（名）
				joal22010101Bean.setStrfilePcAddress(rsSLQ4468.getString(12));						//ＰＣメールアドレス
				joal22010101Bean.setStrfileKtAddress(rsSLQ4468.getString(13));						//携帯メールアドレス
				joal22010101Bean.setStrfileTelNo(rsSLQ4468.getString(14));							//電話番号
				joal22010101Bean.setStrfileKougyoCD(rsSLQ4468.getString(15));						//興行コード
				joal22010101Bean.setStrfileKougyoSubCD(rsSLQ4468.getString(16));					//興行サブコード
				joal22010101Bean.setStrfileUkeInfoCD(rsSLQ4468.getString(17));						//受付情報コード
				joal22010101Bean.setStrfileKouenCD(rsSLQ4468.getString(18));						//公演コード
				joal22010101Bean.setStrfileKougyoName(rsSLQ4468.getString(19));						//興行名称
				joal22010101Bean.setStrfileUkeInfoName(rsSLQ4468.getString(20));					//受付情報名称
				joal22010101Bean.setStrfileKouenName(rsSLQ4468.getString(21));						//公演名
				joal22010101Bean.setStrfileKouenBi(rsSLQ4468.getString(22));						//公演日
				joal22010101Bean.setStrfileOdUkeDate(rsSLQ4468.getString(23));						//オーダー受付日時
				joal22010101Bean.setStrfileOdKubun(rsSLQ4468.getString(24));						//オーダー区分
				joal22010101Bean.setStrfileOdState(rsSLQ4468.getString(25));						//オーダーステータス
				joal22010101Bean.setStrfileUkeChanel(rsSLQ4468.getString(26));						//受付チャネル
				joal22010101Bean.setStrfileUkeSight(rsSLQ4468.getString(27));						//受付サイト
				joal22010101Bean.setStrfileUkeTokudenNo(rsSLQ4468.getString(28));					//受付特電番号
				joal22010101Bean.setStrfileHasshinTelNo(rsSLQ4468.getString(29));					//発信者電話番号
				joal22010101Bean.setStrfileMoshikomiTelNo(rsSLQ4468.getString(30));					//申込電話番号
				joal22010101Bean.setStrfileTkKubun(rsSLQ4468.getString(31));						//特約区分
				joal22010101Bean.setStrfileShiharai(rsSLQ4468.getString(32));						//今回支払方法
				joal22010101Bean.setStrfileUketori(rsSLQ4468.getString(33));						//今回受取方法
				joal22010101Bean.setStrfileSendNo1(rsSLQ4468.getString(34));						//発送先住所（郵便番号１）
				joal22010101Bean.setStrfileSendNo2(rsSLQ4468.getString(35));						//発送先住所（郵便番号２）
				joal22010101Bean.setStrfileTodouhukenCD(rsSLQ4468.getString(36));					//発送先住所（都道府県コード）
				joal22010101Bean.setStrfileShikutyouson(rsSLQ4468.getString(37));					//発送先住所（市区町村）
				joal22010101Bean.setStrfileTyoumeBanchi(rsSLQ4468.getString(38));					//発送先住所（丁目番地）
				joal22010101Bean.setStrfileApartment(rsSLQ4468.getString(39));						//発送先住所（マンション）
				joal22010101Bean.setStrfileZasekiAreaCD(rsSLQ4468.getString(40));					//座席エリアコード
				joal22010101Bean.setStrfileSekisyuCD(rsSLQ4468.getString(41));						//席種コード
				joal22010101Bean.setStrfileSekisyuKubun(rsSLQ4468.getString(42));					//席種区分
				joal22010101Bean.setStrfileMoshikomiMaisu(String.valueOf(rsSLQ4468.getInt(43)));	//申込枚数（席種計）
				joal22010101Bean.setStrfileRetsuNo(String.valueOf(rsSLQ4468.getInt(44)));			//列番
				joal22010101Bean.setStrfileSekiNo(String.valueOf(rsSLQ4468.getInt(45)));			//席番
				joal22010101Bean.setStrfileSekizokuCD1(rsSLQ4468.getString(46));					//席属コード１
				joal22010101Bean.setStrfileSekizokuCD2(rsSLQ4468.getString(47));					//席属コード２
				joal22010101Bean.setStrfileSekizokuCD3(rsSLQ4468.getString(48));					//席属コード３
				joal22010101Bean.setStrfileSekizokuCD4(rsSLQ4468.getString(49));					//席属コード４

				retLstNaiyou.add(joal22010101Bean);

			}
		} finally {
			dbAccesserSLQ4468.close();
		}
		return retLstNaiyou;
	}


	/**
	 * csvファイル出力用データ取得処理(通常出力データが取得できない場合)
	 * @param 	inAutoBoundID       アウトバンド
	 * @param 	dataBean       		データBeanクラス
	 * @return ArrayList			アウトバウンドID
	 * @throws JOFGyomuException	業務アプリで致命的エラー(異常)を検知した場合
	 * @throws SQLException		DBアクセス処理で致命的エラー(異常)を検知した場合
	 */
	public ArrayList getcsvFileDataListSec(String inOutBndID) throws JOFGyomuException, SQLException {

		ArrayList retLstNaiyou = new ArrayList();
		JOFDBAccesser dbAccesserSLQ4486 = new JOFDBAccesser();
		ArrayList conSLQ4486 = new ArrayList(1);
		ResultSet rsSLQ4486 = null;

		conSLQ4486.add(inOutBndID);

		try {
			rsSLQ4486 = dbAccesserSLQ4486.getResultSetBySelect("SLQ4486", conSLQ4486);
			while (rsSLQ4486.next()) {

				JOAL22010101Bean joal22010101Bean = new JOAL22010101Bean();

				/** csvファイル取込用 */
				joal22010101Bean.setStrfileOutBndID(rsSLQ4486.getString(1));						//アウトバウンドID
				joal22010101Bean.setStrfileKanriNo(String.valueOf(rsSLQ4486.getInt(2)));			//管理番号
				joal22010101Bean.setStrfileKibouNo(String.valueOf(rsSLQ4486.getInt(3)));			//希望番号
				joal22010101Bean.setStrfileTkKanriNo(String.valueOf(rsSLQ4486.getInt(4)));			//チケット管理番号
				joal22010101Bean.setStrfileKnInKubun(rsSLQ4486.getString(5));						//会員／一見区分
				joal22010101Bean.setStrfileSoshikiCD(rsSLQ4486.getString(6));						//組織コード
				joal22010101Bean.setStrfileKainID(rsSLQ4486.getString(7));							//会員ID
				joal22010101Bean.setStrfileSimei(rsSLQ4486.getString(8));							//氏名（姓）
				joal22010101Bean.setStrfileMeimei(rsSLQ4486.getString(9));							//氏名（名）
				joal22010101Bean.setStrfileKanaSimei(rsSLQ4486.getString(10));						//カナ氏名（姓）
				joal22010101Bean.setStrfileKanaMeimei(rsSLQ4486.getString(11));						//カナ氏名（名）
				joal22010101Bean.setStrfilePcAddress(rsSLQ4486.getString(12));						//ＰＣメールアドレス
				joal22010101Bean.setStrfileKtAddress(rsSLQ4486.getString(13));						//携帯メールアドレス
				joal22010101Bean.setStrfileTelNo(rsSLQ4486.getString(14));							//電話番号
				joal22010101Bean.setStrfileKougyoCD(rsSLQ4486.getString(15));						//興行コード
				joal22010101Bean.setStrfileKougyoSubCD(rsSLQ4486.getString(16));					//興行サブコード
				joal22010101Bean.setStrfileUkeInfoCD(rsSLQ4486.getString(17));						//受付情報コード
				joal22010101Bean.setStrfileKouenCD(rsSLQ4486.getString(18));						//公演コード
				joal22010101Bean.setStrfileOdState(rsSLQ4486.getString(19));						//オーダーステータス
				joal22010101Bean.setStrfileShiharai(rsSLQ4486.getString(20));						//今回支払方法
				joal22010101Bean.setStrfileUketori(rsSLQ4486.getString(21));						//今回受取方法
				joal22010101Bean.setStrfileSendNo1(rsSLQ4486.getString(22));						//発送先住所（郵便番号１）
				joal22010101Bean.setStrfileSendNo2(rsSLQ4486.getString(23));						//発送先住所（郵便番号２）
				joal22010101Bean.setStrfileTodouhukenCD(rsSLQ4486.getString(24));					//発送先住所（都道府県コード）
				joal22010101Bean.setStrfileShikutyouson(rsSLQ4486.getString(25));					//発送先住所（市区町村）
				joal22010101Bean.setStrfileTyoumeBanchi(rsSLQ4486.getString(26));					//発送先住所（丁目番地）
				joal22010101Bean.setStrfileApartment(rsSLQ4486.getString(27));						//発送先住所（マンション）
				joal22010101Bean.setStrfileZasekiAreaCD(rsSLQ4486.getString(28));					//座席エリアコード
				joal22010101Bean.setStrfileSekisyuCD(rsSLQ4486.getString(29));						//席種コード
				joal22010101Bean.setStrfileSekisyuKubun(rsSLQ4486.getString(30));					//席種区分
				joal22010101Bean.setStrfileMoshikomiMaisu(String.valueOf(rsSLQ4486.getInt(31)));	//申込枚数（席種計）
				joal22010101Bean.setStrfileRetsuNo(String.valueOf(rsSLQ4486.getInt(32)));			//列番
				joal22010101Bean.setStrfileSekiNo(String.valueOf(rsSLQ4486.getInt(33)));			//席番
				joal22010101Bean.setStrfileSekizokuCD1(rsSLQ4486.getString(34));					//席属コード１
				joal22010101Bean.setStrfileSekizokuCD2(rsSLQ4486.getString(35));					//席属コード２
				joal22010101Bean.setStrfileSekizokuCD3(rsSLQ4486.getString(36));					//席属コード３
				joal22010101Bean.setStrfileSekizokuCD4(rsSLQ4486.getString(37));					//席属コード４
				retLstNaiyou.add(joal22010101Bean);

			}
		} finally {
			dbAccesserSLQ4486.close();
		}
		return retLstNaiyou;
	}

	/**
	 * アウトバウンド管理の削除処理を行います。
	 * @param 	 strAutoBoundID		 アウトバウンドＩＤ
	 * @throws  JOFGyomuException   業務共通部品で致命的エラー(異常)を検知した場合
	 * @throws  SQLException        DBアクセス処理で致命的エラー(異常)を検知した場合
	 */
	public void deleteSLQ4469(String strAutoBoundID) throws JOFGyomuException, SQLException {

		JOFDBAccesser dbAccesserSLQ4469 = null;
		ArrayList conSLQ4469 = null;
		ResultSet rsSLQ4469 = null;

		//DBアクセス
		dbAccesserSLQ4469 = new JOFDBAccesser();
		conSLQ4469 = new ArrayList(1);
		conSLQ4469.add(strAutoBoundID);

		//アウトバウンド対象者の削除
		try {
			rsSLQ4469 = dbAccesserSLQ4469.getResultSetBySelect("SLQ4469", conSLQ4469);
		} finally {
			dbAccesserSLQ4469.close();
		}
	}

	/**
	 * ＬＴアウトバウンド管理新規登録処理を行います。
	 * @param 	dataBean      	 	データBeanクラス
	 * @throws JOFGyomuException	業務アプリで致命的エラー(異常)を検知した場合
	 */
	public void insertSLQ4470(L.apl.web.JOAL220101Bean dataBean) throws JOFGyomuException {

		ComboBox cmbTemplate = dataBean.getCmbTemplateKensaku();

		/** 改善-12675（【現行継続】1to1メール更改）Start */

		//ArrayList cond = new ArrayList(19);
		ArrayList cond = new ArrayList(20);

		/** 改善-12675（【現行継続】1to1メール更改）End */

		String strRiyousya = null;
		JCMCheckUtil		checkUtil			= new JCMCheckUtil();

		try {
			//利用者名称の取得
			strRiyousya = getRiyousyaName();
		}catch(Exception e){
			return;
		}

		// アウトバウンドＩＤ
		cond.add(dataBean.getStrOutBndID().getText());
		// アウトバウンド要因ＩＤ
		cond.add(getYouyinID(dataBean));
		// アウトバウンド依頼日
		cond.add(JCMClock.getBussinessDateTime().substring(0, 8));
		// 配信希望日
		cond.add(dataBean.getStrHaisinkibou().replaceAll("/", "").trim());
		// 配信ステータスＩＤ
		cond.add("001");
		// 使用テンプレートＩＤ
		cond.add(dataBean.getCmbTemplateKensaku().getValueAt(dataBean.getCmbTemplateKensaku().getSelectedIndex()));
		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "☆使用テンプレートＩＤ：" + dataBean.getCmbTemplateKensaku().getValueAt(dataBean.getCmbTemplateKensaku().getSelectedIndex()));
		// 登録日時
		cond.add(JCMClock.getBussinessDateTime());
		// 登録担当者＿社員ＩＤ
		cond.add(JOFUtil.getRegUserID());
		// 登録担当者＿社員名
		cond.add(strRiyousya);
		// 更新日時
		cond.add(JCMClock.getBussinessDateTime());
		// 更新担当者＿社員ＩＤ
		cond.add(JOFUtil.getRegUserID());
		// 更新担当者＿社員名
		cond.add(strRiyousya);
		//メモ欄
		cond.add(dataBean.getStrMemo().getText());

		//払戻有無フラグ
		if(dataBean.getChkHaraimodoshiUmu().getChecked()){
			cond.add("1");
		}else{
			cond.add("0");
		}
		//払戻区分(ラジオ釦)
		int uKubun = dataBean.getButlHaraimodoshiInfo().getSelectedIndex();
		if(uKubun == -1){
			cond.add("0");
		}else{
			String hUriKubun = dataBean.getButlHaraimodoshiInfo().getValueAt(dataBean.getButlHaraimodoshiInfo().getSelectedIndex());
			cond.add(hUriKubun);
		}
		//再販売＿売り止め区分(ラジオ釦)
		int hKubun = dataBean.getButlHaraimodoshiNaiyo().getSelectedIndex();
		if(hKubun == -1){
			cond.add("0");
		}else{
			String hBackKubun = dataBean.getButlHaraimodoshiNaiyo().getValueAt(dataBean.getButlHaraimodoshiNaiyo().getSelectedIndex());
			cond.add(hBackKubun);
		}

		//エラー戻りOB要フラグ
		if(dataBean.getChkErrBack().getChecked()){
			cond.add("1");
		}else{
			cond.add("0");
		}
		//原稿チェック有無フラグ
		if(dataBean.getChkGenkouCheck().getChecked()){
			cond.add("1");
		}else{
			cond.add("0");
		}
		//購入者抽出有フラグ
		if(dataBean.getChkBuyerTyusyutsu().getChecked()){
			cond.add("1");
		}else{
			cond.add("0");
		}
		//返信要フラグ
		if(dataBean.getChkHenshinUmu().getChecked()){
			cond.add("1");
		}else{
			cond.add("0");
		}
		//返信日
		cond.add(dataBean.getStrHenshinbi().replaceAll("/","").trim());
		//返信内容
		cond.add(dataBean.getStrHenshinNaiyo().getText());

		// システム管理項目
		cond.add(JOFUtil.getRegUserID());
		cond.add(CLASS_NAME);
		cond.add(JOFUtil.getConsoleID());
		cond.add(JOFUtil.getRegUserID());
		cond.add(CLASS_NAME);
		cond.add(JOFUtil.getConsoleID());

		//優先配信フラグ
		if(dataBean.getChkHaishinYusen().getChecked()){
			cond.add("1");
		}else{
			cond.add("0");
		}

		// ＳＱＬ発行
		JOFDBAccesser dba = new JOFDBAccesser();
		dba.execute("SLQ4470", cond);
	}

	/**
	 * 登録対象データ有無チェック（アウトバウンド管理テーブル）
	 * @param 	strAutoBoundID		アウトバウンドID
	 * @param 	strKubun			ＰＣ＿携帯＿ＴＥＬ区分
	 * @return int					更新対象データ有無フラグ
	 * @throws JOFGyomuException	業務アプリで致命的エラー(異常)を検知した場合
	 * @throws	SQLException		DBアクセス処理で致命的エラー(異常)を検知した場合
	 */
	public int getCntOutBndKanriData(String strOutBndID) throws JOFGyomuException, SQLException {

		JOFDBAccesser	dbAccesserSLQ4471	= new JOFDBAccesser();
		ArrayList		conSLQ4471			= new ArrayList();
		ResultSet		rsSLQ4471			= null;
		int intCount = 0;

		conSLQ4471.add(strOutBndID);

		//ＬＴアウトバウンド管理からアウトバウンドの存在チェック
		try {
			rsSLQ4471 = dbAccesserSLQ4471.getResultSetBySelect("SLQ4471", conSLQ4471);

			if (rsSLQ4471.next()) {
				intCount++;
			}
		} finally {
			dbAccesserSLQ4471.close();
		}

		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "★intCount: " + intCount);
		return intCount;
	}

	/**
	 * 更新対象データ有無チェック（アウトバウンド送信内容テーブル）
	 * @param 	strAutoBoundID		アウトバウンドID
	 * @param 	strKubun			ＰＣ＿携帯＿ＴＥＬ区分
	 * @return int					更新対象データ有無フラグ
	 * @throws JOFGyomuException	業務アプリで致命的エラー(異常)を検知した場合
	 * @throws	SQLException		DBアクセス処理で致命的エラー(異常)を検知した場合
	 */
	public int getUpdateData(String strAutoBoundID, String strKubun) throws JOFGyomuException, SQLException {

		// PC/携帯/TEL区分ごとの件数を取得する
		int iFlg = -1;

		JOFDBAccesser dbAccesserSLQ2380 = null;
		dbAccesserSLQ2380 = new JOFDBAccesser();
		ArrayList conSLQ2380 = new ArrayList();
		ResultSet rsSLQ2380 = null;
		int intCount = 0;

		conSLQ2380.add(strAutoBoundID);
		conSLQ2380.add(strKubun);

		//ＬＴアウトバウンド管理からアウトバウンドの存在チェック
		try {
			rsSLQ2380 = dbAccesserSLQ2380.getResultSetBySelect("SLQ2380", conSLQ2380);

			if (rsSLQ2380.next()) {
				intCount++;
			}
		} finally {
			dbAccesserSLQ2380.close();
		}

		return intCount;
	}

	/**
	 * アウトバウンドIDの存在チェック。
	 * @param 	 strAutoBoundID 	 アウトバウンドID
	 * @return  intCount     		 データ数
	 * @throws  JOFGyomuException   業務共通部品で致命的エラー(異常)を検知した場合
	 * @throws  SQLException        DBアクセス処理で致命的エラー(異常)を検知した場合
	 */
	public int getOutBndIDCnt(String strAutoBoundID) throws JOFGyomuException, SQLException {

		JOFDBAccesser dbAccesserSLQ9410 = null;
		ArrayList conSLQ9410 = null;
		ResultSet rsSLQ9410 = null;

		String strStatusID = null;
		int intCount = 0;

		//DBアクセス
		dbAccesserSLQ9410 = new JOFDBAccesser();

		conSLQ9410 = new ArrayList(1);
		conSLQ9410.add(strAutoBoundID);

		//ＬＴアウトバウンド管理からアウトバウンドの存在チェック
		try {
			rsSLQ9410 = dbAccesserSLQ9410.getResultSetBySelect("SLQ9410", conSLQ9410);

			if (rsSLQ9410.next()) {
				intCount++;
			}
		} finally {
			dbAccesserSLQ9410.close();
		}

		return intCount;
	}

	/**
	 * アウトバウンドIDの存在チェック。
	 * @param 	 strAutoBoundID 	 アウトバウンドID
	 * @return  intCount     		 データ数
	 * @throws  JOFGyomuException   業務共通部品で致命的エラー(異常)を検知した場合
	 * @throws  SQLException        DBアクセス処理で致命的エラー(異常)を検知した場合
	 */
	public int getAutoBoundCnt(String strAutoBoundID) throws JOFGyomuException, SQLException {

		JOFDBAccesser dbAccesserSLQ9410 = null;
		ArrayList conSLQ9410 = null;
		ResultSet rsSLQ9410 = null;

		String strStatusID = null;
		int intCount = 0;

		//DBアクセス
		dbAccesserSLQ9410 = new JOFDBAccesser();

		conSLQ9410 = new ArrayList(1);
		conSLQ9410.add(strAutoBoundID);

		//ＬＴアウトバウンド管理からアウトバウンドの存在チェック
		try {
			rsSLQ9410 = dbAccesserSLQ9410.getResultSetBySelect("SLQ9410", conSLQ9410);

			if (rsSLQ9410.next()) {
				intCount++;
			}
		} finally {
			dbAccesserSLQ9410.close();
		}

		return intCount;
	}

	/**
	 * 配信ステータスＩＤの取得処理。
	 * @param 	 strAutoBoundID 	 アウトバウンドID
	 * @return  strStatusID     	 配信ステータスＩＤ
	 * @throws  JOFGyomuException   業務共通部品で致命的エラー(異常)を検知した場合
	 * @throws  SQLException        DBアクセス処理で致命的エラー(異常)を検知した場合
	 */
	public String getHaishinStatusID(String strAutoBoundID) throws JOFGyomuException, SQLException {

		JOFDBAccesser dbAccesserSLQ9410 = null;
		ArrayList conSLQ9410 = null;
		ResultSet rsSLQ9410 = null;

		String strStatusID = null;

		//DBアクセス
		dbAccesserSLQ9410 = new JOFDBAccesser();

		conSLQ9410 = new ArrayList(1);
		conSLQ9410.add(strAutoBoundID);

		//ＬＴアウトバウンド管理から配信ステータスＩＤを取得する
		try {
			rsSLQ9410 = dbAccesserSLQ9410.getResultSetBySelect("SLQ9410", conSLQ9410);

			if (rsSLQ9410.next()) {
				strStatusID = rsSLQ9410.getString(1);
			}
		} finally {
			dbAccesserSLQ9410.close();
		}

		return strStatusID;
	}

	/**
	 * /詳細レコード通番のMAXを取得します。
	 * @param 	inOutBndID			アウトバウンドID
	 * @return String				詳細レコード通番のMAX
	 * @throws JOFGyomuException	業務アプリで致命的エラー(異常)を検知した場合
	 * @throws SQLException		DBアクセス処理で致命的エラー(異常)を検知した場合
	 */
	public String getMaxRecordNo_Tsuban(String inOutBndID) throws JOFGyomuException, SQLException {

		String retMaxNo = null;
		JOFDBAccesser dbAccesserSLQ4472 = null;
		ArrayList conSLQ4472 = null;
		ResultSet rsSLQ4472 = null;

		//DBアクセス
		dbAccesserSLQ4472 = new JOFDBAccesser();
		conSLQ4472 = new ArrayList(1);
		conSLQ4472.add(inOutBndID);

		try {
			rsSLQ4472 = dbAccesserSLQ4472.getResultSetBySelect("SLQ4472", conSLQ4472);
			if (rsSLQ4472.next()) {
				retMaxNo = rsSLQ4472.getString(1);
			}

		} finally {
			dbAccesserSLQ4472.close();
		}
		return retMaxNo;
	}

	/**
	 * /詳細レコード通番のMAXを取得します。
	 * @param 	inOutBndID			アウトバウンドID
	 * @return String				詳細レコード通番のMAX
	 * @throws JOFGyomuException	業務アプリで致命的エラー(異常)を検知した場合
	 * @throws SQLException		DBアクセス処理で致命的エラー(異常)を検知した場合
	 */
	public String getMaxRecordNo_Renban(String inOutBndID) throws JOFGyomuException, SQLException {

		String retMaxNo = null;
		JOFDBAccesser dbAccesserSLQ4473 = null;
		ArrayList conSLQ4473 = null;
		ResultSet rsSLQ4473 = null;

		//DBアクセス
		dbAccesserSLQ4473 = new JOFDBAccesser();
		conSLQ4473 = new ArrayList(1);
		conSLQ4473.add(inOutBndID);

		try {
			rsSLQ4473 = dbAccesserSLQ4473.getResultSetBySelect("SLQ4473", conSLQ4473);
			if (rsSLQ4473.next()) {
				retMaxNo = rsSLQ4473.getString(1);
			}

		} finally {
			dbAccesserSLQ4473.close();
		}
		return retMaxNo;
	}

	/**
	 * 抽出表示件数を取得してBeanにセットします
	 * @param dataBean データBeanクラス
	 * @return
	 * @throws JOFGyomuException	業務アプリで致命的エラー(異常)を検知した場合
	 * @throws SQLException		DBアクセス処理で致命的エラー(異常)を検知した場合
	 */
	public JOAL220101Bean getTyusyutuKensuOutIDAri(L.apl.web.JOAL220101Bean dataBean) throws JOFGyomuException, SQLException {

		ArrayList retLstNaiyou = new ArrayList();
		JOFDBAccesser dbAccesserSLQ4475 = new JOFDBAccesser();
		ArrayList conSLQ4475 = new ArrayList(1);
		ResultSet rsSLQ4475 = null;
		int iNo = 0;

		conSLQ4475.add(dataBean.getStrOutBndID().getText());
		conSLQ4475.add(dataBean.getStrJcbOutBndID().getText());
		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "☆SLQ4475パラ1" +dataBean.getStrOutBndID().getText());
		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "☆SLQ4475パラ2" +dataBean.getStrJcbOutBndID().getText());

		try {
			rsSLQ4475 = dbAccesserSLQ4475.getResultSetBySelect("SLQ4475", conSLQ4475);
			while (rsSLQ4475.next()) {
				JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "☆rsSLQ4475.getInt(1)" +String.valueOf(rsSLQ4475.getInt(1)));
				JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "☆rsSLQ4475.getInt(2)" +String.valueOf(rsSLQ4475.getInt(2)));
				JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "☆rsSLQ4475.getInt(3)" +String.valueOf(rsSLQ4475.getInt(3)));
				dataBean.setStrPCNum(String.valueOf(rsSLQ4475.getInt(1)));
				dataBean.setStrKeitaiNum(String.valueOf(rsSLQ4475.getInt(2)));
				dataBean.setStrSonotaNum(String.valueOf(rsSLQ4475.getInt(3)));
				// 総件数
				dataBean.setStrAllNum(String.valueOf(rsSLQ4475.getInt(1) + rsSLQ4475.getInt(2)));
				dataBean.setStrhidPCNum(String.valueOf(rsSLQ4475.getInt(1)));
				dataBean.setStrhidKeitaiNum(String.valueOf(rsSLQ4475.getInt(2)));
				dataBean.setStrhidSonotaNum(String.valueOf(rsSLQ4475.getInt(3)));
			}
		} finally {
			dbAccesserSLQ4475.close();
		}
		return(dataBean);
	}

	/**
	 * (受付チャネルから)PC,携帯、その他の合計数の計算処理を行います。
	 * @param 	ArrayList	ユーザー情報リスト
	 * @return int[]		PC,携帯,その他の合計数
	 */
	public int[] getWuketukechannelCount(ArrayList lstUserInfo) {

		Map listMap = new HashMap();
		JCMLQToiawaseRegistry toiawaseregistry = new JCMLQToiawaseRegistry();

		//PC,携帯、その他の合計数の計算。
		int[] retCount = new int[3];
		retCount[0] = 0;
		retCount[1] = 0;
		retCount[2] = 0;

		String pcAddress = "";
		String ktAddress = "";
		String telNo	 = "";

		int intDataCnt = 0;
		for (int count = 0; count < lstUserInfo.size(); count++) {
			JOAL22010101Bean joal22010101Bean = (JOAL22010101Bean) lstUserInfo.get(count);

			if(pcAddress.equals("0")){		//ＰＣ
				retCount[0]++;
			}else if(pcAddress.equals("1")){	//携帯
				retCount[1]++;
			}else{							//その他
				retCount[2]++;
			}

			if(ktAddress.equals("0")){		//ＰＣ
				retCount[0]++;
			}else if(ktAddress.equals("1")){	//携帯
				retCount[1]++;
			}else{							//その他
				retCount[2]++;
			}

			if(telNo.equals("0")){		//ＰＣ
				retCount[0]++;
			}else if(telNo.equals("1")){	//携帯
				retCount[1]++;
			}else{							//その他
				retCount[2]++;
			}
		}
		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "区分別数 ＰＣ　: " + retCount[0]);
		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "区分別数 携帯　: " + retCount[1]);
		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "区分別数 その他: " + retCount[2]);
		return retCount;
	}

	/**
	 * テンプレート情報取得する処理を行います。
	 * @return String[][]			テンプレート情報
	 * @throws JOFGyomuException	業務アプリで致命的エラー(異常)を検知した場合
	 * @throws SQLException		DBアクセス処理で致命的エラー(異常)を検知した場合
	 */
	public String[][] getTemplateJoho() throws JOFGyomuException, SQLException {

		String[][] retTemplateInfo = null;
		JCMCheckUtil checkUtil = new JCMCheckUtil();
		JOFDBAccesser dbAccesserSLQ2040 = null;
		ArrayList conSLQ2040 = null;
		ResultSet rsSLQ2040 = null;
		String strIdValue = null;
		String strNameValue = null;
		String strTitleValue = null;

		//カテゴリＩＤリスト
		ArrayList lstIDValue = new ArrayList();
		//テンプレート種別リスト
		ArrayList lstNameValue = new ArrayList();
		//タイトルリスト
		ArrayList lstTitleValue = new ArrayList();

		int intRSCount = 0;

		//DBアクセス
		dbAccesserSLQ2040 = new JOFDBAccesser();
		conSLQ2040 = new ArrayList();

		try {
			rsSLQ2040 = dbAccesserSLQ2040.getResultSetBySelect("SLQ2040", conSLQ2040);

			while (rsSLQ2040.next()) {
				//取得した値がnullの場合、""に設定する
				if (checkUtil.isCheckExist(rsSLQ2040.getString(3))) {
					strIdValue = rsSLQ2040.getString(3);
				} else {
					strIdValue = "";
				}

				if (checkUtil.isCheckExist(rsSLQ2040.getString(2))) {
					strNameValue = rsSLQ2040.getString(2);
				} else {
					strNameValue = "";
				}

				if (checkUtil.isCheckExist(rsSLQ2040.getString(1))) {
					strTitleValue = rsSLQ2040.getString(1);
				} else {
					strTitleValue = "";
				}

				//カテゴリＩＤ
				lstIDValue.add(intRSCount, strIdValue);
				//テンプレート種別
				lstNameValue.add(intRSCount, strNameValue);
				//タイトル
				lstTitleValue.add(intRSCount, strTitleValue);

				intRSCount++;
			}

			if (intRSCount > 0) { //取得件数は1の以上場合
				retTemplateInfo = new String[lstNameValue.size()][3];
				for (int intRetCount = 0; intRetCount < lstNameValue.size(); intRetCount++) {
					retTemplateInfo[intRetCount][1] = (String) lstNameValue.get(intRetCount);
					retTemplateInfo[intRetCount][2] = (String) lstTitleValue.get(intRetCount);
					retTemplateInfo[intRetCount][0] = (String) lstIDValue.get(intRetCount);
				}
			}

		} finally {
			dbAccesserSLQ2040.close();
		}
		return retTemplateInfo;
	}

	/**
	 * テンプレート情報を取得する処理を行います。
	 * @param 	inTemplateID		テンプレートID
	 * @return String[]			テンプレート情報
	 * @throws JOFGyomuException	業務アプリで致命的エラー(異常)を検知した場合
	 * @throws SQLException		DBアクセス処理で致命的エラー(異常)を検知した場合
	 */
	public String[] getTemplateInfo(String inTemplateID) throws JOFGyomuException, SQLException {

		String[] retHonbun = null;
		JOFDBAccesser dbAccesserSLQ2180 = null;
		ArrayList conSLQ2180 = null;
		ResultSet rsSLQ2180 = null;

		//DBアクセス
		dbAccesserSLQ2180 = new JOFDBAccesser();
		conSLQ2180 = new ArrayList(1);
		conSLQ2180.add(inTemplateID);

		try {
			rsSLQ2180 = dbAccesserSLQ2180.getResultSetBySelect("SLQ2180", conSLQ2180);
			if (rsSLQ2180.next()) {
				retHonbun = new String[4];
				retHonbun[0] = rsSLQ2180.getString(1);
				retHonbun[1] = rsSLQ2180.getString(2);
				retHonbun[2] = rsSLQ2180.getString(3);
				retHonbun[3] = rsSLQ2180.getString(4);
			}

		} finally {
			dbAccesserSLQ2180.close();
		}
		return retHonbun;
	}

	/**
	 * テンプレート情報を取得する処理を行います。
	 * @param 	inTemplateID		テンプレートID
	 * @return String[]			テンプレート情報
	 * @throws JOFGyomuException	業務アプリで致命的エラー(異常)を検知した場合
	 * @throws SQLException		DBアクセス処理で致命的エラー(異常)を検知した場合
	 */
	public HashMap<Language, JOALMailTemplate> getInboundTemplateInfo(String inTemplateID) throws JOFGyomuException, SQLException {
		HashMap<Language, JOALMailTemplate> result = new HashMap<>();
		JOFDBAccesser dba = new JOFDBAccesser();
		ArrayList con = new ArrayList(1);
		con.add(inTemplateID);

		try {
			ResultSet rs = dba.getResultSetBySelect("SLQ2181", con);
			while (rs.next()) {
				JOALMailTemplate mt = new JOALMailTemplate();
				mt.setKenmei(rs.getString(2));
				mt.setNaiyou(rs.getString(3));
				result.put(Language.get(rs.getString(1)), mt);
			}
			return result;
		} catch (Exception e) {
			throw e;
		}finally {
			dba.close();
		}
	}

	/**
	 * アウトバウンド送信内容を取得する処理を行います。
	 * @param 	dataBean			データBeanクラス
	 * @return JOAL220101Bean		アウトバウンド送信内容
	 * @throws JOFGyomuException	業務アプリで致命的エラー(異常)を検知した場合
	 * @throws SQLException		DBアクセス処理で致命的エラー(異常)を検知した場合
	 */
	public JOAL220101Bean getSoushinNaiyou(L.apl.web.JOAL220101Bean dataBean) throws Exception,JOFGyomuException, SQLException {

		JOFDBAccesser 		dbAccessor		= null;
		ArrayList 			con				= null;
		ResultSet 			rsSLQ4476				= null;
		JCMCheckUtil		checkUtil				= new JCMCheckUtil();

		//DBアクセス
		dbAccessor = new JOFDBAccesser();
		con = new ArrayList(1);
		con.add(dataBean.getStrOutBndID().getText());
		con.add(dataBean.getStrJcbOutBndID().getText());

		try {
			rsSLQ4476 = dbAccessor.getResultSetBySelect("SLQ4476", con);
			dataBean.setStrKenmei(new FieldString(""));
			dataBean.setStrSousinNaiyou(new FieldTextArea(""));
			dataBean.setStrKenmeiKeitai(new FieldString(""));
			dataBean.setStrSousinNaiyokeitai(new FieldTextArea(""));
			dataBean.setStrTyusyutuRecordFlg(g_TyusyutuNasi);


			int i = 1;
			while (rsSLQ4476.next()) {
				JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "☆SLQ4476取得件数      :"+ i++);
				JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "☆ＰＣ＿携帯＿ＴＥＬ区分      :"+ rsSLQ4476.getString(1));
				dataBean.setStrTyusyutuRecordFlg(g_TyusyutuAru);
				if (pcFlag.equals(rsSLQ4476.getString(1))) {
					dataBean.setStrKenmei(new FieldString(rsSLQ4476.getString(2)));
					dataBean.setStrSousinNaiyou(new FieldTextArea(rsSLQ4476.getString(3)));
					dataBean.setGstrReplyTo(rsSLQ4476.getString(4));
					dataBean.setStrMemo(new FieldTextArea(rsSLQ4476.getString(5)));
				} else if (kaitaiFlag.equals(rsSLQ4476.getString(1))) {
					dataBean.setStrKenmeiKeitai(new FieldString(rsSLQ4476.getString(2)));
					dataBean.setStrSousinNaiyokeitai(new FieldTextArea(rsSLQ4476.getString(3)));
					dataBean.setGstrReplyTo(rsSLQ4476.getString(4));
					dataBean.setStrMemo(new FieldTextArea(rsSLQ4476.getString(5)));
				}

				//払戻有無フラグ
				if(rsSLQ4476.getString(6).equals("1")){
					dataBean.getChkHaraimodoshiUmu().setChecked(true);
				}else{
					dataBean.getChkHaraimodoshiUmu().setChecked(false);
				}
				//払戻区分(ラジオ釦)
				if(!rsSLQ4476.getString(7).equals("0")){
					dataBean.getButlHaraimodoshiNaiyo().select(rsSLQ4476.getString(8));
				}else{
					dataBean.getButlHaraimodoshiNaiyo().select("");
				}
				//再販売＿売り止め区分(ラジオ釦)
				if(!rsSLQ4476.getString(8).equals("0")){
					dataBean.getButlHaraimodoshiInfo().select(rsSLQ4476.getString(7));
				}else{
					dataBean.getButlHaraimodoshiInfo().select("");
				}
				//エラー戻りOB要フラグ
				if(rsSLQ4476.getString(9).equals("1")){
					dataBean.getChkErrBack().setChecked(true);
				}else{
					dataBean.getChkErrBack().setChecked(false);
				}
				//原稿チェック有無フラグ
				if(rsSLQ4476.getString(10).equals("1")){
					dataBean.getChkGenkouCheck().setChecked(true);
				}else{
					dataBean.getChkGenkouCheck().setChecked(false);
				}
				//購入者抽出有フラグ
				if(rsSLQ4476.getString(11).equals("1")){
					dataBean.getChkBuyerTyusyutsu().setChecked(true);
				}else{
					dataBean.getChkBuyerTyusyutsu().setChecked(false);
				}
				//返信要フラグ
				if(rsSLQ4476.getString(12).equals("1")){
					dataBean.getChkHenshinUmu().setChecked(true);
				}else{
					dataBean.getChkHenshinUmu().setChecked(false);
				}

				//返信日
				if(checkUtil.isCheckExist(rsSLQ4476.getString(13))){

					String year = rsSLQ4476.getString(13).substring(0,4);
					String month = rsSLQ4476.getString(13).substring(4,6);
					String date = rsSLQ4476.getString(13).substring(6,8);
					JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "返信日加工処理前year: " + year);
					JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "返信日加工処理前month: " + month);
					JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "返信日加工処理前date: " + date);

					String changeDate = year+"/"+month+"/"+date;

					JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "返信日加工処理後: " + changeDate);
					dataBean.setStrHenshinbi(changeDate);
				}else{
					dataBean.setStrHenshinbi("");
				}

				//返信内容
				if(checkUtil.isCheckExist(rsSLQ4476.getString(14))){
					dataBean.getStrHenshinNaiyo().setText(rsSLQ4476.getString(14));
				}else{
					dataBean.getStrHenshinNaiyo().setText("");
				}

				//返信要フラグ
				if(rsSLQ4476.getString(15).equals("1")){
					dataBean.getChkHaishinYusen().setChecked(true);
					dataBean.setChkHaishinYusenCom(true);			//変更比較用
				}else{
					dataBean.getChkHaishinYusen().setChecked(false);
					dataBean.setChkHaishinYusenCom(false);			//変更比較用
				}

			}

			// インバウンド購入者宛
			if (hasInboundOrder(dataBean.getStrOutBndID().getText())) {
				dataBean.setToShowLanguageSelect(true);
				HashMap<Language, JOALMailTemplate> inboundInfo = getInboundOutboundInfo(dataBean.getStrOutBndID().getText(), dataBean.getStrJcbOutBndID().getText());
				setInboundOutboundInfo(dataBean, inboundInfo);
				Map<Language, Integer> inboundCount = getInboundTargetCount(dataBean.getStrOutBndID().getText());
				dataBean.setInboundTargetCount(inboundCount);
				setLanguageTargetCount(dataBean);
				setSelectedLanguage(dataBean);
			} else {
				dataBean.setToShowLanguageSelect(false);
			}
		}catch(Exception ex){
			JCMLog.errorLog(JCMLQConstants.ERROR_LOG_LEVEL, "ML1008E", "送信内容取得エラー", ex);
		} finally {
			dbAccessor.close();
		}
		return dataBean;
	}

	/**
	 * 送信情報を取得する処理を行います。
	 * @param 	inAutoBoundID      	アウトバウンドID
	 * @return String[]			送信情報
	 * @throws JOFGyomuException	業務アプリで致命的エラー(異常)を検知した場合
	 * @throws SQLException		DBアクセス処理で致命的エラー(異常)を検知した場合
	 */
	public String[] getSousinInfo(String inAutoBoundID) throws JOFGyomuException, SQLException {

		String[] retSousinInfo = new String[4];
		JOFDBAccesser dbAccesserSLQ2310 = null;
		ArrayList conSLQ2310 = null;
		ResultSet rsSLQ2310 = null;

		//DBアクセス
		dbAccesserSLQ2310 = new JOFDBAccesser();
		conSLQ2310 = new ArrayList(3);
		conSLQ2310.add(inAutoBoundID);

		try {
			rsSLQ2310 = dbAccesserSLQ2310.getResultSetBySelect("SLQ2311", conSLQ2310);

			if (rsSLQ2310.next()) {
				retSousinInfo[0] = rsSLQ2310.getString(1);
				retSousinInfo[1] = rsSLQ2310.getString(2);
				retSousinInfo[2] = rsSLQ2310.getString(3);
				retSousinInfo[3] = rsSLQ2310.getString(4);
			}
		} finally {
			dbAccesserSLQ2310.close();
		}
		return retSousinInfo;
	}

	/**
	 * 受付情報を取得する処理を行います。
	 * @param 	inGyoukouCode 		興行コード
	 * @param 	inSubCode 			興行コードサブ
	 * @return String[][]			受付情報
	 * @throws JOFGyomuException	業務アプリで致命的エラー(異常)を検知した場合
	 * @throws SQLException		DBアクセス処理で致命的エラー(異常)を検知した場合
	 */
	public String[][] getUketukeJoho(String inGyoukouCode, String inSubCode) throws JOFGyomuException, SQLException {
		//受付情報を取得する
		String[][] retUketukeJoho = null;
		JCMCheckUtil checkUtil = new JCMCheckUtil();
		JOFDBAccesser dbAccesserSLQ2060 = null;
		ArrayList conSLQ2060 = null;
		ResultSet rsSLQ2060 = null;
		ArrayList lstIDValue = new ArrayList();
		ArrayList lstNameValue = new ArrayList();
		int intRSCount = 0;
		String strWhereCondition = null;
		String strWuketukeCode = null;
		String strWuketukeMeiSyo = null;

		//DBアクセス
		dbAccesserSLQ2060 = new JOFDBAccesser();
		conSLQ2060 = new ArrayList();

		//検索条件
		strWhereCondition = " Where \"興行コード\" = ? AND \"興行サブコード\" IN (";
		conSLQ2060.add(inGyoukouCode);

		StringTokenizer tokenizer = new StringTokenizer(inSubCode, ",");
		StringBuffer buffer = new StringBuffer();

		while(tokenizer.hasMoreTokens()){
			strWhereCondition = strWhereCondition + "?,";
			conSLQ2060.add(tokenizer.nextToken());
		}

		strWhereCondition = strWhereCondition.substring(0, strWhereCondition.length() - 1) + ") ORDER BY \"受付名称ＢＡＣＫ用\" ASC";

		try {
			rsSLQ2060 = dbAccesserSLQ2060.getResultSetBySelect("SLQ2060", conSLQ2060, strWhereCondition);

			while (rsSLQ2060.next()) {
				//受付情報コード
				if (checkUtil.isCheckExist(rsSLQ2060.getString(1))) {
					strWuketukeCode = rsSLQ2060.getString(1);
				} else {
					strWuketukeCode = "";
				}
				lstIDValue.add(intRSCount, strWuketukeCode);
				//受付名称ＢＡＣＫ用
				if (checkUtil.isCheckExist(rsSLQ2060.getString(2))) {
					strWuketukeMeiSyo = rsSLQ2060.getString(2);
				} else {
					strWuketukeMeiSyo = "";
				}

				lstNameValue.add(intRSCount, strWuketukeMeiSyo);
				intRSCount++;
			}
			if (intRSCount > 0) { //取得件数は1の以上場合
				retUketukeJoho = new String[lstNameValue.size()][2];
				for (int intRetCount = 0; intRetCount < lstNameValue.size(); intRetCount++) {
					retUketukeJoho[intRetCount][1] = (String) lstNameValue.get(intRetCount);
					retUketukeJoho[intRetCount][0] = (String) lstIDValue.get(intRetCount);
				}
			}

		} finally {
			dbAccesserSLQ2060.close();
		}
		return retUketukeJoho;
	}

	/**
	 * 受付情報を取得する処理を行います。
	 * @param 	inGyoukouCode 		興行コード
	 * @param 	inSubCode 			興行コードサブ
	 * @return String[][]			受付情報
	 * @throws JOFGyomuException	業務アプリで致命的エラー(異常)を検知した場合
	 * @throws SQLException		DBアクセス処理で致命的エラー(異常)を検知した場合
	 */
	public String[][] getKougyouJoho(String inGyoukouCode, String inSubCode) throws JOFGyomuException, SQLException {

		//受付情報を取得する
		String[][] retKougyouJoho = null;
		JCMCheckUtil checkUtil = new JCMCheckUtil();
		JOFDBAccesser dbAccesserSLQ2031 = null;
		ArrayList conSLQ2031 = null;
		ResultSet rsSLQ2031 = null;
		ArrayList lstIDValue = new ArrayList();
		ArrayList lstNameValue = new ArrayList();
		int intRSCount = 0;
		String strWhereCondition = null;

		String strKougyouSubCode = null;
		String strKougyouMei = null;

		//DBアクセス
		dbAccesserSLQ2031 = new JOFDBAccesser();
		conSLQ2031 = new ArrayList();

		//検索条件
		strWhereCondition = " Where \"興行コード\" = ? AND \"興行サブコード\" IN (";
		conSLQ2031.add(inGyoukouCode);

		StringTokenizer tokenizer = new StringTokenizer(inSubCode, ",");
		StringBuffer buffer = new StringBuffer();

		while(tokenizer.hasMoreTokens()){
			strWhereCondition = strWhereCondition + "?,";
			conSLQ2031.add(tokenizer.nextToken());
		}

		strWhereCondition = strWhereCondition.substring(0, strWhereCondition.length() - 1) + ") ORDER BY \"興行名称１\" ASC";

		try {
			rsSLQ2031 = dbAccesserSLQ2031.getResultSetBySelect("SLQ2031", conSLQ2031, strWhereCondition);

			while (rsSLQ2031.next()) {
				//興行サブコード
				if (checkUtil.isCheckExist(rsSLQ2031.getString(1))) {
					strKougyouSubCode = rsSLQ2031.getString(1);
					JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "SLQ2031 (興行サブコード) " + "[" + strKougyouSubCode + "]");
				} else {
					strKougyouSubCode = "";
				}
				lstIDValue.add(intRSCount, strKougyouSubCode);
				//受付名称ＢＡＣＫ用
				if (checkUtil.isCheckExist(rsSLQ2031.getString(2))) {
					strKougyouMei = rsSLQ2031.getString(2);
					JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "SLQ2031 (興行名称１) " + "[" + strKougyouMei + "]");
				} else {
					strKougyouMei = "";
				}

				lstNameValue.add(intRSCount, strKougyouMei);
				intRSCount++;
			}
			if (intRSCount > 0) { //取得件数は1の以上場合
				retKougyouJoho = new String[lstNameValue.size()][2];
				for (int intRetCount = 0; intRetCount < lstNameValue.size(); intRetCount++) {
					retKougyouJoho[intRetCount][1] = (String) lstNameValue.get(intRetCount);
					retKougyouJoho[intRetCount][0] = (String) lstIDValue.get(intRetCount);
					JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "SLQ2031 (リスト１) " + "[" + retKougyouJoho[intRetCount][1] + "]");
					JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "SLQ2031 (リスト０) " + "[" + retKougyouJoho[intRetCount][0] + "]");
				}
			}

		} finally {
			dbAccesserSLQ2031.close();
		}
		return retKougyouJoho;
	}

	/**
	 * 受付チャネルを取得する処理を行います。
	 * @param	syutokuinfo	取得情報
	 * @return	String[][]	オーダーステータスまたは要因情報
	 */
	public String[][] getUketsukeChanelInfo() {

		String[][] retInfo = null;

		//受付チャネル情報を取得
		String[][] retUketsukeChanel = null;
		ArrayList lstInfo = (ArrayList) JCMAppellationUtility.getAppellationInfoList("1037");

		if (lstInfo != null) {

			retUketsukeChanel = new String[lstInfo.size()][2];

			for (int count = 0; count < lstInfo.size(); count++) {
				JCMAppellationInfo infoYouyin = (JCMAppellationInfo) lstInfo.get(count);
				//名称識別IDの取得
				retUketsukeChanel[count][0] = infoYouyin.getAppellationID();
				//名称の取得
				retUketsukeChanel[count][1] = infoYouyin.getAppellation();
			}
			retInfo = retUketsukeChanel;
		}

		return retInfo;
	}

	/**
	 * オーダーステータス情報または要因情報を取得する処理を行います。
	 * @param	syutokuinfo	取得情報
	 * @return	String[][]	オーダーステータスまたは要因情報
	 */
	public String[][] getOrderStatusInfoOrYouyinInfo(String syutokuinfo) {

		String[][] retInfo = null;

		//オーダーステータス情報を取得
		if (syutokuinfo == "status") {

			String[][] retOrderStatusInfo = null;
			ArrayList lstInfo = (ArrayList) JCMAppellationUtility.getAppellationInfoList("1003");

			if (lstInfo != null) {

				retOrderStatusInfo = new String[lstInfo.size()][2];

				for (int count = 0; count < lstInfo.size(); count++) {
					JCMAppellationInfo infoYouyin = (JCMAppellationInfo) lstInfo.get(count);
					//名称識別IDの取得
					retOrderStatusInfo[count][0] = infoYouyin.getAppellationID();
					//名称の取得
					retOrderStatusInfo[count][1] = infoYouyin.getAppellation();
				}
				retInfo = retOrderStatusInfo;
			}

		}
		//要因情報を取得
		if (syutokuinfo == "youyin") {

			String[][] retYouyinInfo = null;
			ArrayList lstInfo = (ArrayList) JCMAppellationUtility.getAppellationInfoList("864");

			if (lstInfo != null) {

				retYouyinInfo = new String[lstInfo.size()][2];

				for (int count = 0; count < lstInfo.size(); count++) {
					JCMAppellationInfo infoYouyin = (JCMAppellationInfo) lstInfo.get(count);
					//称識別IDの取得
					retYouyinInfo[count][0] = infoYouyin.getAppellationID();
					//名称の取得
					retYouyinInfo[count][1] = infoYouyin.getAppellation();
				}
				retInfo = retYouyinInfo;
			}
		}
		return retInfo;
	}

	/**
	 * 今回支払方法を取得する処理を行います。
	 * @param	syutokuinfo	取得情報
	 * @return	String[][]	オーダーステータスまたは要因情報
	 */
	public String[][] getCmdData(String strNameID) {

		String[][] retInfo = null;

		//コンボ情報を取得
		String[][] getInfo = null;
		ArrayList lstInfo = (ArrayList) JCMAppellationUtility.getAppellationInfoList(strNameID);

		if (lstInfo != null) {

			getInfo = new String[lstInfo.size()][2];

			for (int count = 0; count < lstInfo.size(); count++) {
				JCMAppellationInfo infoYouyin = (JCMAppellationInfo) lstInfo.get(count);
				//名称識別IDの取得
				getInfo[count][0] = infoYouyin.getAppellationID();
				//名称の取得
				getInfo[count][1] = infoYouyin.getAppellation();
			}
			retInfo = getInfo;
		}

		return retInfo;
	}

	/**
	 * 公演情報を取得する処理を行います。
	 * @param 	inGyoukouCode 		興行コード
	 * @param 	inSubCode			興行コードサブ
	 * @return String[][]			公演情報
	 * @throws JOFGyomuException	業務アプリで致命的エラー(異常)を検知した場合
	 * @throws SQLException		DBアクセス処理で致命的エラー(異常)を検知した場合
	 */
	public String[][] getKouenInfo(String inGyoukouCode, String inSubCode) throws JOFGyomuException, SQLException {
		//公演情報取得
		String[][] retKouenJoho = null;
		JCMCheckUtil checkUtil = new JCMCheckUtil();
		String strIdValue = null;
		String strNameValue = null;
		String strDateValue = null;
		JOFDBAccesser dbAccesserSLQ2030 = null;
		ArrayList conSLQ2030 = null;
		ResultSet rsSLQ2030 = null;

		ArrayList lstIDValue = new ArrayList();
		ArrayList lstNameValue = new ArrayList();
		ArrayList lstDateValue = new ArrayList();

		int intRSCount = 0;
		String strWhereCondition = null;

		//DBアクセス
		dbAccesserSLQ2030 = new JOFDBAccesser();
		conSLQ2030 = new ArrayList();

		//検索条件設定
		strWhereCondition = " Where \"興行コード\" = ? AND \"興行サブコード\" IN (";

		conSLQ2030.add(inGyoukouCode);
		StringTokenizer tokenizer = new StringTokenizer(inSubCode, ",");
		StringBuffer buffer = new StringBuffer();

		while (tokenizer.hasMoreTokens()) {
			strWhereCondition = strWhereCondition + "?,";
			conSLQ2030.add(tokenizer.nextToken());
		}

		strWhereCondition = strWhereCondition.substring(0, strWhereCondition.length() - 1) + ")) Order by \"公演日\" ASC";

		try {
			rsSLQ2030 = dbAccesserSLQ2030.getResultSetBySelect("SLQ2030", conSLQ2030, strWhereCondition);

			while (rsSLQ2030.next()) {
				//取得した値がnullの場合、""に設定する
				if (checkUtil.isCheckExist(rsSLQ2030.getString(1))) {
					strIdValue = rsSLQ2030.getString(1);
				} else {
					strIdValue = "";
				}

				if (checkUtil.isCheckExist(rsSLQ2030.getString(2))) {
					strNameValue = rsSLQ2030.getString(2);
				} else {
					strNameValue = "";
				}

				if (checkUtil.isCheckExist(rsSLQ2030.getString(3))) {
					strDateValue = rsSLQ2030.getString(3);
				} else {
					strDateValue = "";
				}

				//"公演コード"
				lstIDValue.add(intRSCount, strIdValue);
				//"公演名"
				lstNameValue.add(intRSCount, strNameValue);
				//"公演日"
				lstDateValue.add(intRSCount, strDateValue);

				intRSCount++;
			}

			if (intRSCount > 0) { //取得件数は1の以上場合
				retKouenJoho = new String[lstNameValue.size()][3];
				for (int intRetCount = 0; intRetCount < lstNameValue.size(); intRetCount++) {
					retKouenJoho[intRetCount][1] = (String) lstNameValue.get(intRetCount);
					retKouenJoho[intRetCount][2] = (String) lstDateValue.get(intRetCount);
					retKouenJoho[intRetCount][0] = (String) lstIDValue.get(intRetCount);
				}
			}

		} finally {
			dbAccesserSLQ2030.close();
		}
		return retKouenJoho;
	}

	/**
	 * アウトバウンド送信内容を取得する処理を行います。(hidden用)
	 * @param 	dataBean			データBeanクラス
	 * @return JOAL220101Bean		アウトバウンド送信内容
	 * @throws JOFGyomuException	業務アプリで致命的エラー(異常)を検知した場合
	 * @throws SQLException		DBアクセス処理で致命的エラー(異常)を検知した場合
	 */
	public JOAL220101Bean getSoushinNaiyouHidden(L.apl.web.JOAL220101Bean dataBean) throws Exception,JOFGyomuException, SQLException {

		JOFDBAccesser 		dbAccessor		= null;
		ArrayList 			con				= null;
		ResultSet 			rsSLQ4476				= null;
		JCMCheckUtil		checkUtil				= new JCMCheckUtil();

		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "■getSoushinNaiyouHidden開始");
		//DBアクセス
		dbAccessor = new JOFDBAccesser();
		con = new ArrayList(1);
		con.add(dataBean.getStrOutBndID().getText());
		con.add(dataBean.getStrJcbOutBndID().getText());

		try {
			rsSLQ4476 = dbAccessor.getResultSetBySelect("SLQ4476", con);

			dataBean.getStrhidKenmei().setText("");
			dataBean.getStrhidSousinNaiyou().setText("");
			dataBean.getStrhidKenmeiKeitai().setText("");
			dataBean.getStrhidSousinNaiyokeitai().setText("");
			dataBean.setStrhidReplyTo("");
			dataBean.getStrhidMemo().setText("");
			dataBean.setChkhidHaraimodoshiUmu(false);
			dataBean.setStrhidHaraimodoshiNaiyo("");
			dataBean.setStrhidHaraimodoshiInfo("");
			dataBean.setChkhidErrBack(false);
			dataBean.setChkhidGenkouCheck(false);
			dataBean.setChkhidBuyerTyusyutsu(false);
			dataBean.setChkhidHenshinUmu(false);
			dataBean.setStrhidHenshinbi("");

			while (rsSLQ4476.next()) {

				JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "rsSLQ4476.getString(1) : " + rsSLQ4476.getString(1));
				JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "rsSLQ4476.getString(2) : " + rsSLQ4476.getString(2));
				JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "rsSLQ4476.getString(3) : " + rsSLQ4476.getString(3));
				JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "rsSLQ4476.getString(4) : " + rsSLQ4476.getString(4));
				JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "rsSLQ4476.getString(5) : " + rsSLQ4476.getString(5));
				JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "rsSLQ4476.getString(6) : " + rsSLQ4476.getString(6));
				JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "rsSLQ4476.getString(7) : " + rsSLQ4476.getString(7));
				JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "rsSLQ4476.getString(8) : " + rsSLQ4476.getString(8));
				JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "rsSLQ4476.getString(9) : " + rsSLQ4476.getString(9));
				JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "rsSLQ4476.getString(10): " + rsSLQ4476.getString(10));
				JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "rsSLQ4476.getString(11): " + rsSLQ4476.getString(11));
				JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "rsSLQ4476.getString(12): " + rsSLQ4476.getString(12));
				JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "rsSLQ4476.getString(13): " + rsSLQ4476.getString(13));
				JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "rsSLQ4476.getString(14): " + rsSLQ4476.getString(14));

				dataBean.setStrTyusyutuRecordFlg(g_TyusyutuAru);
				if (pcFlag.equals(rsSLQ4476.getString(1))) {
					dataBean.getStrhidKenmei().setText(rsSLQ4476.getString(2));
					dataBean.getStrhidSousinNaiyou().setText(rsSLQ4476.getString(3));
					dataBean.setStrhidReplyTo(rsSLQ4476.getString(4));
					dataBean.getStrhidMemo().setText(rsSLQ4476.getString(5));
				} else if (kaitaiFlag.equals(rsSLQ4476.getString(1))) {
					dataBean.getStrhidKenmeiKeitai().setText(rsSLQ4476.getString(2));
					dataBean.getStrhidSousinNaiyokeitai().setText(rsSLQ4476.getString(3));
					dataBean.setStrhidReplyTo(rsSLQ4476.getString(4));
					dataBean.getStrhidMemo().setText(rsSLQ4476.getString(5));
				}

				//払戻有無フラグ
				if(rsSLQ4476.getString(6).equals("1")){
					dataBean.setChkhidHaraimodoshiUmu(true);
				}
				//払戻区分(ラジオ釦)
				if(!rsSLQ4476.getString(7).equals("0")){
					dataBean.setStrhidHaraimodoshiNaiyo(rsSLQ4476.getString(8));
				}
				//再販売＿売り止め区分(ラジオ釦)
				if(!rsSLQ4476.getString(8).equals("0")){
					dataBean.setStrhidHaraimodoshiInfo(rsSLQ4476.getString(7));
				}
				//エラー戻りOB要フラグ
				if(rsSLQ4476.getString(9).equals("1")){
					dataBean.setChkhidErrBack(true);
				}
				//原稿チェック有無フラグ
				if(rsSLQ4476.getString(10).equals("1")){
					dataBean.setChkhidGenkouCheck(true);
				}
				//購入者抽出有フラグ
				if(rsSLQ4476.getString(11).equals("1")){
					dataBean.setChkhidBuyerTyusyutsu(true);
				}
				//返信要フラグ
				if(rsSLQ4476.getString(12).equals("1")){
					dataBean.setChkhidHenshinUmu(true);
				}

				//返信日
				if(checkUtil.isCheckExist(rsSLQ4476.getString(13))){

					String year = rsSLQ4476.getString(13).substring(0,4);
					String month = rsSLQ4476.getString(13).substring(4,6);
					String date = rsSLQ4476.getString(13).substring(6,8);
					String changeDate = year+"/"+month+"/"+date;
					JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "返信日加工処理後: " + changeDate);
					dataBean.setStrhidHenshinbi(changeDate);
				}

				//返信内容
				if(checkUtil.isCheckExist(rsSLQ4476.getString(14))){
					dataBean.getStrhidHenshinNaiyo().setText(rsSLQ4476.getString(14));
					JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "返信内容："+dataBean.getStrhidHenshinNaiyo().getText());
				}
			}

			// インバウンド用
			if (dataBean.isToShowLanguageSelect()) {
				HashMap<Language, JOALMailTemplate> inboundInfo = getInboundOutboundInfo(dataBean.getStrOutBndID().getText(), dataBean.getStrJcbOutBndID().getText());
				setInboundOutboundInfoHidden(dataBean, inboundInfo);
			}

		}catch(Exception ex){
			JCMLog.errorLog(JCMLQConstants.ERROR_LOG_LEVEL, "ML1008E", "送信内容の取得に失敗しました。", ex);
		} finally {
			dbAccessor.close();
		}
		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "■getSoushinNaiyouHidden終了");
		return dataBean;
	}

	/**
	 * アウトバウンド対象者の削除処理を行います。
	 * @param 	 strAutoBoundID		 アウトバウンドＩＤ
	 * @throws  JOFGyomuException   業務共通部品で致命的エラー(異常)を検知した場合
	 * @throws  SQLException        DBアクセス処理で致命的エラー(異常)を検知した場合
	 */
	public void doDelete(String strAutoBoundID) throws JOFGyomuException, SQLException {

		JOFDBAccesser dbAccesserSLQ9420 = null;
		ArrayList conSLQ9420 = null;
		ResultSet rsSLQ9420 = null;

		//DBアクセス
		dbAccesserSLQ9420 = new JOFDBAccesser();

		conSLQ9420 = new ArrayList(1);
		conSLQ9420.add(strAutoBoundID);

		//アウトバウンド対象者の削除
		try {
			rsSLQ9420 = dbAccesserSLQ9420.getResultSetBySelect("SLQ9420", conSLQ9420);

		} finally {
			dbAccesserSLQ9420.close();
		}
	}

	/**
	 * アウトバウンド対象者詳細の削除処理を行います。
	 * @param 	 strAutoBoundID		 アウトバウンドＩＤ
	 * @throws  JOFGyomuException   業務共通部品で致命的エラー(異常)を検知した場合
	 * @throws  SQLException        DBアクセス処理で致命的エラー(異常)を検知した場合
	 */
	public void SyosaiDelete(String inOutBndID) throws JOFGyomuException, SQLException {

		JOFDBAccesser dbAccesserSLQ4478 = null;
		ArrayList conSLQ4478 = null;
		ResultSet rsSLQ4478 = null;

		//DBアクセス
		dbAccesserSLQ4478 = new JOFDBAccesser();

		conSLQ4478 = new ArrayList(1);
		conSLQ4478.add(inOutBndID);

		//アウトバウンド対象者の削除
		try {
			rsSLQ4478 = dbAccesserSLQ4478.getResultSetBySelect("SLQ4478", conSLQ4478);

		} finally {
			dbAccesserSLQ4478.close();
		}
	}



	/**
	 * アウトバウンド抽出情報の処理を行います。
	 * @param 	inAutoBoundID   	アウトバンド
	 * @return ArrayList			アウトバウンド抽出
	 * @throws JOFGyomuException	業務アプリで致命的エラー(異常)を検知した場合
	 * @throws SQLException		DBアクセス処理で致命的エラー(異常)を検知した場合
	 */
	public ArrayList getAutoBoundSyutuInfo(String inAutoBoundID) throws JOFGyomuException, SQLException {

		//抽出情報
		JOFDBAccesser dbAccesserSLQ2330 = null;
		ArrayList conSLQ2330 = null;
		ResultSet rsSLQ2330 = null;

		//DBアクセス
		dbAccesserSLQ2330 = new JOFDBAccesser();
		conSLQ2330 = new ArrayList(1);

		conSLQ2330.add(inAutoBoundID);

		ArrayList lstTyuutu = new ArrayList();
		JCMCheckUtil oJCMCheckUtil = new JCMCheckUtil();
		int intCount = 0;

		try {
			rsSLQ2330 = dbAccesserSLQ2330.getResultSetBySelect("SLQ2330", conSLQ2330);

			while (rsSLQ2330.next()) {

				JOAL22010102Bean oJOAL22010102Bean = new JOAL22010102Bean();

				oJOAL22010102Bean.setStrAutoBoundID(rsSLQ2330.getString(1));
				oJOAL22010102Bean.setStrTyuusyutuRenban(rsSLQ2330.getString(2));
				oJOAL22010102Bean.setStrUketukeJohoName(rsSLQ2330.getString(3));
				oJOAL22010102Bean.setStrGyoukoumei(rsSLQ2330.getString(4));
				oJOAL22010102Bean.setStrKouenCode(rsSLQ2330.getString(5));
				oJOAL22010102Bean.setStrGyoukouCode(rsSLQ2330.getString(6));
				oJOAL22010102Bean.setStrSubGyoukouCode(rsSLQ2330.getString(7));
				oJOAL22010102Bean.setStrKouenhi(rsSLQ2330.getString(8));

				//開演時刻の取得
				String strKouenJikan = "";

				if (oJCMCheckUtil.isCheckExist(rsSLQ2330.getString(9))) {
					strKouenJikan = strKouenJikan + rsSLQ2330.getString(9);
					if (oJCMCheckUtil.isCheckExist(rsSLQ2330.getString(10))) {
						strKouenJikan = strKouenJikan + ":" + rsSLQ2330.getString(10);
					} else {
						strKouenJikan = strKouenJikan + ":00";
					}
				} else {
					if (oJCMCheckUtil.isCheckExist(rsSLQ2330.getString(10))) {
						strKouenJikan = strKouenJikan + "00:" + rsSLQ2330.getString(10);
					}
				}

				//その他の設定
				oJOAL22010102Bean.setStrKaien(strKouenJikan);
				oJOAL22010102Bean.setStrUketukeJohoCode(rsSLQ2330.getString(11));
				oJOAL22010102Bean.setStrOrderStatus(rsSLQ2330.getString(12));
				oJOAL22010102Bean.setStrOrderName(rsSLQ2330.getString(13));
				lstTyuutu.add(intCount, oJOAL22010102Bean);

				intCount++;
			}

		} finally {
			dbAccesserSLQ2330.close();
		}
		return lstTyuutu;
	}
	/**
	 * 該当アウトバウンドIDの送信ステータスを取得する
	 * @param dataBean
	 * @return  strStatus 001:依頼中、002:送信中、003:対象外
	 * @exception JOFGyomuException
	 * @exception SQLExceptiont
	 */
	public String getStatusFlg(JOAL220101Bean dataBean)throws JOFGyomuException, SQLException {
			String strStatus  = "";

			JOFDBAccesser dbAccesserSLQ9370 = null;
			ArrayList conSLQ9370 = null;
			ResultSet rsSLQ9370 = null;

			//DBアクセス
			dbAccesserSLQ9370 = new JOFDBAccesser();
			conSLQ9370 = new ArrayList(1);

			JCMCheckUtil checkUtil = new JCMCheckUtil();

			String strAutobindId = getAutoBoundID(dataBean);

			if(checkUtil.isCheckExist(strAutobindId)){
				try {
					conSLQ9370.add(strAutobindId);
					rsSLQ9370 = dbAccesserSLQ9370.getResultSetBySelect("SLQ9370", conSLQ9370);

					while (rsSLQ9370.next()) {
						strStatus = rsSLQ9370.getString(1);
					}
				} finally {
					dbAccesserSLQ9370.close();
				}
			}else{
				strStatus = "000";
			}

			return strStatus;
	}

	/**
	 * 受付情報区分数を取得する処理を行います。
	 * @param 	inConditionInfo     コンディション情報
	 * @return int					受付情報区分数
	 * @throws JOFGyomuException	業務アプリで致命的エラー(異常)を検知した場合
	 * @throws SQLException		DBアクセス処理で致命的エラー(異常)を検知した場合
	 */
	public int getUketukeJohoKubunNum(String[] inConditionInfo) throws JOFGyomuException, SQLException {
		//受付情報
		int intRetNum = 0;
		int intNullNum = 0;
		HashMap hmContent = new HashMap();
		StringTokenizer sTokenizer = null;
		String strKougyouCode = null;
		String strSubKougyouCode = null;
		String strUketukeCode = null;

		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "興行、サブ、受付" + Arrays.asList(inConditionInfo));
		for (int count = 0; count < inConditionInfo.length; count++) {
			//興行コード、サプ興行コード、受付情報コードに内容を設定する
			sTokenizer = new StringTokenizer(inConditionInfo[count], ",");
			strKougyouCode = sTokenizer.nextToken();
			strSubKougyouCode = sTokenizer.nextToken();
			strUketukeCode = sTokenizer.nextToken();

			String strRetValue = null;

			//興行コード、サプ興行コード、受付情報コードによって、受付区分を取得する
			strRetValue = getUketukeJohoKubunName(strKougyouCode, strSubKougyouCode, strUketukeCode);

			if (strRetValue != null) {
				//既に取得した受付情報区分と異なる場合は、受付情報区分数をカウントアップする
				if (!hmContent.containsKey(strRetValue)) {
					hmContent.put(strRetValue, strRetValue);
				}
			} else {
				intNullNum++;
			}
		}
		//全部受付情報区分数を計算する
		intRetNum = hmContent.size() + intNullNum;
		return intRetNum;
	}

	/**
	 * 受付情報区分数を取得する処理を行います。
	 * @param 	inConditionInfo     コンディション情報
	 * @return int					受付情報区分数
	 * @throws JOFGyomuException	業務アプリで致命的エラー(異常)を検知した場合
	 * @throws SQLException		DBアクセス処理で致命的エラー(異常)を検知した場合
	 */
	public String[] getUketukeJohoKubun(String[] inConditionInfo) throws JOFGyomuException, SQLException {
		//受付情報
		int intRetNum = 0;
		int intNullNum = 0;
		HashMap hmContent = new HashMap();
		StringTokenizer sTokenizer = null;
		String strKougyouCode = null;
		String strSubKougyouCode = null;
		String strUketukeCode = null;
		String strRetValue = null;
		ArrayList list = new ArrayList();

		for (int count = 0; count < inConditionInfo.length; count++) {
			//興行コード、サプ興行コード、受付情報コードに内容を設定する
			sTokenizer = new StringTokenizer(inConditionInfo[count], ",");
			strKougyouCode = sTokenizer.nextToken();
			strSubKougyouCode = sTokenizer.nextToken();
			strUketukeCode = sTokenizer.nextToken();

			//興行コード、サプ興行コード、受付情報コードによって、受付区分を取得する
			strRetValue = getUketukeJohoKubunName(strKougyouCode, strSubKougyouCode, strUketukeCode);

			if (strRetValue != null) {
				//既に取得した受付情報区分と異なる場合は、受付情報区分数をカウントアップする
				if (!hmContent.containsKey(strRetValue)) {
					hmContent.put(strRetValue, strRetValue);
					list.add(strRetValue);
				}
			} else {
				intNullNum++;
			}
		}
		String[] strUketukekubun = new String[list.size()];
		for (int i = 0; i < list.size(); i++) {
			strUketukekubun[i] = list.get(i).toString();
		}
		return strUketukekubun;
	}

	/**
	 * 受付情報区分名を取得する処理を行います。
	 * @param 	strKougyouCode     	興行コード
	 * @param 	strSubKougyouCode  	サブ興行コード
	 * @param 	strUketukeCode     	受付情報コード
	 * @return String				受付情報区分名
	 * @throws JOFGyomuException	業務アプリで致命的エラー(異常)を検知した場合
	 * @throws SQLException		DBアクセス処理で致命的エラー(異常)を検知した場合
	 */
	private String getUketukeJohoKubunName(String strKougyouCode, String strSubKougyouCode, String strUketukeCode) throws JOFGyomuException, SQLException {

		String retName = null;

		JOFDBAccesser dbAccesserSLQ2340 = null;
		ArrayList conSLQ2340 = null;
		ResultSet rsSLQ2340 = null;
		String[] retBoundInfo = null;

		//DBアクセス
		dbAccesserSLQ2340 = new JOFDBAccesser();
		conSLQ2340 = new ArrayList(3);
		conSLQ2340.add(strKougyouCode);
		conSLQ2340.add(strSubKougyouCode);
		conSLQ2340.add(strUketukeCode);

		try {
			rsSLQ2340 = dbAccesserSLQ2340.getResultSetBySelect("SLQ2340", conSLQ2340);

			while (rsSLQ2340.next()) {
				retName = rsSLQ2340.getString(1);
			}
		} finally {
			dbAccesserSLQ2340.close();
		}
		return retName;
	}

	/**
	 * Remoteファイル取得する処理を行います。
	 * @param 	inputStream	ダウンロードファイルのデータ
	 * @param 	strFileName	ファイル名
	 * @return MimeSource	Remoteファイル
	 */
	public MimeSource getRemoteFile(FileInputStream inputStream, String strFileName) {
		// サーバから送信するファイルと、クライアントで受け取るファイル名を指定します。
		MimeSource mimesource = null;
		try {
			mimesource = new MimeSource(inputStream, strFileName);
			mimesource.setContentType("application/octet-stream");
		} catch (Exception ex) {
			mimesource = null;
		}
		return mimesource;
	}


	/**
	 * アウトバウンド送信内容を取得する処理を行います。
	 * @param 	inAutoBoundID		アウトバウンドID
	 * @param 	dataBean			データBeanクラス
	 * @return JOAL220101Bean		アウトバウンド送信内容
	 * @throws JOFGyomuException	業務アプリで致命的エラー(異常)を検知した場合
	 * @throws SQLException		DBアクセス処理で致命的エラー(異常)を検知した場合
	 */
	public JOAL220101Bean getAutoBoundSousinNaiyou(String inAutoBoundID, L.apl.web.JOAL220101Bean dataBean) throws JOFGyomuException, SQLException {

		JOFDBAccesser dbAccesserSLQ2370 = null;
		ArrayList conSLQ2370 = null;
		ResultSet rsSLQ2370 = null;

		//DBアクセス
		dbAccesserSLQ2370 = new JOFDBAccesser();
		conSLQ2370 = new ArrayList(1);
		conSLQ2370.add(inAutoBoundID);

		try {
			rsSLQ2370 = dbAccesserSLQ2370.getResultSetBySelect("SLQ2370", conSLQ2370);
			dataBean.setStrKenmei(new FieldString(""));
			dataBean.setStrSousinNaiyou(new FieldTextArea(""));
			dataBean.setStrKenmeiKeitai(new FieldString(""));
			dataBean.setStrSousinNaiyokeitai(new FieldTextArea(""));
			dataBean.setStrTyusyutuRecordFlg(g_TyusyutuNasi);
			while (rsSLQ2370.next()) {
				dataBean.setStrTyusyutuRecordFlg(g_TyusyutuAru);
				if (pcFlag.equals(rsSLQ2370.getString(1))) {
					dataBean.setStrKenmei(new FieldString(rsSLQ2370.getString(2)));
					dataBean.setStrSousinNaiyou(new FieldTextArea(rsSLQ2370.getString(3)));
					dataBean.setGstrReplyTo(rsSLQ2370.getString(4));
				} else if (kaitaiFlag.equals(rsSLQ2370.getString(1))) {
					dataBean.setStrKenmeiKeitai(new FieldString(rsSLQ2370.getString(2)));
					dataBean.setStrSousinNaiyokeitai(new FieldTextArea(rsSLQ2370.getString(3)));
					dataBean.setGstrReplyTo(rsSLQ2370.getString(4));
				}
			}

		} finally {
			dbAccesserSLQ2370.close();
		}
		return dataBean;
	}

	/**
	 * ＬＴアウトバウンド送信内容の更新処理を行います。
	 * @param 	dataBean      	 	データBeanクラス
	 * @throws JOFGyomuException	業務アプリで致命的エラー(異常)を検知した場合
	 */
	public void updatePCSLQ2250(L.apl.web.JOAL220101Bean dataBean) throws JOFGyomuException {

		JOFDBAccesser dbAccesserSLQ2250 = null;
		ArrayList conSLQ2250 = null;
		Clob clobObj = null;

		//DBアクセス
		dbAccesserSLQ2250 = new JOFDBAccesser();
		conSLQ2250 = new ArrayList(8);
		conSLQ2250.add(dataBean.getStrKenmei().getText());
		clobObj = JCFDBUtil.stringToClob(dataBean.getStrSousinNaiyou().getText());
		conSLQ2250.add(clobObj);
		ComboBox cmbReplyTo = dataBean.getCmbReplyTo();

		if (cmbReplyTo.getSelectedIndex() > 0) {
			conSLQ2250.add(cmbReplyTo.getTextAt(cmbReplyTo.getSelectedIndex()));
		} else {
			conSLQ2250.add(" ");
		}

		conSLQ2250.add(JOFUtil.getRegUserID());
		conSLQ2250.add(CLASS_NAME);
		conSLQ2250.add(JOFUtil.getConsoleID());
		conSLQ2250.add(getAutoBoundID(dataBean));
		conSLQ2250.add(pc);

		try {
			dbAccesserSLQ2250.execute("SLQ2250", conSLQ2250);
		} finally {
			dbAccesserSLQ2250.close();
		}
	}

	/**
	 * ＬＴアウトバウンド携帯送信内容の更新処理を行います。
	 * @param 	dataBean      	 	データBeanクラス
	 * @throws JOFGyomuException	業務アプリで致命的エラー(異常)を検知した場合
	 */
	public void updateKeitaiSLQ2250(L.apl.web.JOAL220101Bean dataBean) throws JOFGyomuException {

		JOFDBAccesser dbAccesserSLQ2250 = null;
		ArrayList conSLQ2250 = null;
		Clob clobObj = null;

		//DBアクセス
		dbAccesserSLQ2250 = new JOFDBAccesser();
		conSLQ2250 = new ArrayList(8);
		conSLQ2250.add(dataBean.getStrKenmeiKeitai().getText());
		clobObj = JCFDBUtil.stringToClob(dataBean.getStrSousinNaiyokeitai().getText());
		conSLQ2250.add(clobObj);
		ComboBox cmbReplyTo = dataBean.getCmbReplyTo();

		if (cmbReplyTo.getSelectedIndex() > 0) {
			conSLQ2250.add(cmbReplyTo.getTextAt(cmbReplyTo.getSelectedIndex()));
		} else {
			conSLQ2250.add("");
		}

		conSLQ2250.add(JOFUtil.getRegUserID());
		conSLQ2250.add(CLASS_NAME);
		conSLQ2250.add(JOFUtil.getConsoleID());
		conSLQ2250.add(getAutoBoundID(dataBean));
		conSLQ2250.add(keitai);

		try {
			dbAccesserSLQ2250.execute("SLQ2250", conSLQ2250);
		} finally {
			dbAccesserSLQ2250.close();
		}
	}

	/**
	 * ＬＴアウトバウンドメールリンクの追加処理を行います。
	 * @param 	dataBean      	 	データBeanクラス
	 * @param  strKouyou		 	固有番号
	 * @throws JOFGyomuException	業務アプリで致命的エラー(異常)を検知した場合
	 */
	public void insertPCSLQ2350(L.apl.web.JOAL220101Bean dataBean, String strKouyou) throws JOFGyomuException {

		JOFDBAccesser dbAccesserSLQ2350 = null;
		ArrayList conSLQ2350 = null;

		//DBアクセス
		dbAccesserSLQ2350 = new JOFDBAccesser();
		conSLQ2350 = new ArrayList(9);
		conSLQ2350.add(getAutoBoundID(dataBean));
		conSLQ2350.add(pc);
		conSLQ2350.add(strKouyou);
		conSLQ2350.add(JOFUtil.getRegUserID());
		conSLQ2350.add(CLASS_NAME);
		conSLQ2350.add(JOFUtil.getConsoleID());
		conSLQ2350.add(JOFUtil.getRegUserID());
		conSLQ2350.add(CLASS_NAME);
		conSLQ2350.add(JOFUtil.getConsoleID());

		try {
			dbAccesserSLQ2350.execute("SLQ2350", conSLQ2350);
		} finally {
			dbAccesserSLQ2350.close();
		}
	}

	/**
	 * ＬＴアウトバウンドメールリンク（携帯）の追加処理を行います。
	 * @param 	dataBean      	 	データBeanクラス
	 * @param  strKouyou		 	固有番号
	 * @throws JOFGyomuException	業務アプリで致命的エラー(異常)を検知した場合
	 */
	public void insertKeitaiSLQ2350(L.apl.web.JOAL220101Bean dataBean, String strKouyou) throws JOFGyomuException {

		JOFDBAccesser dbAccesserSLQ2350 = null;
		ArrayList conSLQ2350 = null;

		//DBアクセス
		dbAccesserSLQ2350 = new JOFDBAccesser();
		conSLQ2350 = new ArrayList(9);
		conSLQ2350.add(getAutoBoundID(dataBean));
		conSLQ2350.add(keitai);
		conSLQ2350.add(strKouyou);
		conSLQ2350.add(JOFUtil.getRegUserID());
		conSLQ2350.add(CLASS_NAME);
		conSLQ2350.add(JOFUtil.getConsoleID());
		conSLQ2350.add(JOFUtil.getRegUserID());
		conSLQ2350.add(CLASS_NAME);
		conSLQ2350.add(JOFUtil.getConsoleID());

		try {
			dbAccesserSLQ2350.execute("SLQ2350", conSLQ2350);
		} finally {
			dbAccesserSLQ2350.close();
		}
	}

	/**
	 * ＬＴアウトバウンド管理の配信ステータス変更処理を行います。
	 * @param 	dataBean      	 	データBeanクラス
	 * @throws JOFGyomuException	業務アプリで致命的エラー(異常)を検知した場合
	 */
	public void updateSLQ2260(L.apl.web.JOAL220101Bean dataBean) throws JOFGyomuException {

		JOFDBAccesser dbAccesserSLQ2260 = null;
		ArrayList	conSLQ2260	= null;
		String		strRiyousya	=null;

		try {
			//利用者名称の取得
			strRiyousya = getRiyousyaName();
		}catch(Exception e){
			return;
		}

		//DBアクセス
		dbAccesserSLQ2260 = new JOFDBAccesser();
		conSLQ2260 = new ArrayList(8);

		conSLQ2260.add(dataBean.getStrSousinStatusID());
		conSLQ2260.add(JCMClock.getBussinessDateTime());
		conSLQ2260.add(JOFUtil.getRegUserID());
		conSLQ2260.add(strRiyousya);
		conSLQ2260.add(JOFUtil.getRegUserID());
		conSLQ2260.add(CLASS_NAME);
		conSLQ2260.add(JOFUtil.getConsoleID());
		conSLQ2260.add(getAutoBoundID(dataBean));

		try {
			//SLQ2260を実行する
			dbAccesserSLQ2260.execute("SLQ2260", conSLQ2260);

		} finally {
			dbAccesserSLQ2260.close();
		}
	}

	/**
	 * アウトバウンドＩＤを取得する処理を行います。
	 * @param 	dataBean	データBeanクラス
	 * @return	String		アウトバウンドID
	 */
	public String getAutoBoundID(JOAL220101Bean dataBean) {

		JCMCheckUtil oJCMCheckUtil = new JCMCheckUtil();
		String retAutoBoundID = null;

		if (oJCMCheckUtil.isCheckExist(dataBean.getStrOutBndID().getText())) {
			retAutoBoundID = dataBean.getStrOutBndID().getText();
		} else {
			retAutoBoundID = "";
		}
		return retAutoBoundID;
	}

	/**
	 * アウトバウンドSequenceを取得する処理を行います。
	 * @param 	dataBean      	 	データBeanクラス
	 * @throws JOFGyomuException	業務アプリで致命的エラー(異常)を検知した場合
	 * @throws SQLException		DBアクセス処理で致命的エラー(異常)を検知した場合
	 */
	public void getAutoBoundSequence(L.apl.web.JOAL220101Bean dataBean) throws JOFGyomuException, SQLException {

		JOFDBAccesser dbAccesserSLQ2190 = null;
		ArrayList conSLQ2190 = null;

		dbAccesserSLQ2190 = new JOFDBAccesser();
		ResultSet rsSLQ2190 = null;

		try {
			rsSLQ2190 = dbAccesserSLQ2190.getResultSetBySelect("SLQ2190", conSLQ2190);
			while (rsSLQ2190.next()) {
				String strAutoBound = rsSLQ2190.getString(1);
				int intLen = strAutoBound.length();
				for (int count = 0; count < 8 - intLen; count++) {
					strAutoBound = strAutoBound + " ";
				}
				dataBean.setStrhidTaisyouAutoboundID(strAutoBound);
				dataBean.getStrOutBndID().setText(strAutoBound);
			}
		} finally {
			dbAccesserSLQ2190.close();
		}
	}

//JCb対応 start
	/**
	 * JCBアウトバウンドSequenceを取得する処理を行います。
	 * @param 	dataBean      	 	データBeanクラス
	 * @throws JOFGyomuException	業務アプリで致命的エラー(異常)を検知した場合
	 * @throws SQLException		DBアクセス処理で致命的エラー(異常)を検知した場合
	 */
	public void getJcbAutoBoundSequence(L.apl.web.JOAL220101Bean dataBean) throws JOFGyomuException, SQLException {

		JOFDBAccesser dbAccesserSLQ2191 = null;
		ArrayList conSLQ2191 = null;

		//DBアクセス
		dbAccesserSLQ2191 = new JOFDBAccesser();
		ResultSet rsSLQ2191 = null;

		try {
			rsSLQ2191 = dbAccesserSLQ2191.getResultSetBySelect("SLQ2191", conSLQ2191);
			while (rsSLQ2191.next()) {
				String strAutoBound = rsSLQ2191.getString(1);
				//１桁目に「J」を挿入
				strAutoBound = "J" + strAutoBound;

				int intLen = strAutoBound.length();
				for (int count = 0; count < 8 - intLen; count++) {
					strAutoBound = strAutoBound + " ";
				}
				dataBean.setStrhidTaisyouAutoboundID(strAutoBound);
				dataBean.getStrJcbOutBndID().setText(strAutoBound);
			}
		} finally {
			dbAccesserSLQ2191.close();
		}
	}
//JCb対応 end
	/**
	 * アウトバウンドSequenceを取得する処理を行います。(ファイルBean用)
	 * @param 	dataBean      	 	データBeanクラス
	 * @throws JOFGyomuException	業務アプリで致命的エラー(異常)を検知した場合
	 * @throws SQLException		DBアクセス処理で致命的エラー(異常)を検知した場合
	 */
	public void getOutBndIDSequenceFileTorikomi(L.apl.web.JOAL22010101Bean dataBean) throws JOFGyomuException, SQLException {

		JOFDBAccesser dbAccesserSLQ2190 = null;
		ArrayList conSLQ2190 = null;

		//DBアクセス
		dbAccesserSLQ2190 = new JOFDBAccesser();
		ResultSet rsSLQ2190 = null;

		try {
			rsSLQ2190 = dbAccesserSLQ2190.getResultSetBySelect("SLQ2190", conSLQ2190);
			while (rsSLQ2190.next()) {
				String strOutBnd = rsSLQ2190.getString(1);
				int intLen = strOutBnd.length();
				for (int count = 0; count < 8 - intLen; count++) {
					strOutBnd = strOutBnd + " ";
				}
				JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "☆シーケンスより取得したアウトバウンドID:" + strOutBnd);
				dataBean.setStrfileOutBndID(strOutBnd);
			}
		} finally {
			dbAccesserSLQ2190.close();
		}
	}

	/**
	 * JCBアウトバウンドSequenceを取得する処理を行います。(ファイルBean用)
	 * @param 	dataBean      	 	データBeanクラス
	 * @throws JOFGyomuException	業務アプリで致命的エラー(異常)を検知した場合
	 * @throws SQLException		DBアクセス処理で致命的エラー(異常)を検知した場合
	 */
	public void getJcbOutBndIDSequenceFileTorikomi(L.apl.web.JOAL22010101Bean dataBean) throws JOFGyomuException, SQLException {

		JOFDBAccesser dbAccesserSLQ2191 = null;
		ArrayList conSLQ2191 = null;

		//DBアクセス
		dbAccesserSLQ2191 = new JOFDBAccesser();
		ResultSet rsSLQ2191 = null;

		try {
			//SLQ2191を実行する
			rsSLQ2191 = dbAccesserSLQ2191.getResultSetBySelect("SLQ2191", conSLQ2191);
			while (rsSLQ2191.next()) {
				String strOutBnd = rsSLQ2191.getString(1);
				//１桁目に「J」を挿入
				strOutBnd = "J" + strOutBnd;

				int intLen = strOutBnd.length();
				for (int count = 0; count < 8 - intLen; count++) {
					strOutBnd = strOutBnd + " ";
				}
				JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "☆シーケンスより取得したJCBアウトバウンドID:" + strOutBnd);
				dataBean.setStrfileOutBndID(strOutBnd);
			}
		} finally {
			dbAccesserSLQ2191.close();
		}
	}

	/**
	 * ＬＴアウトバウンド対象者新規登録処理を行います。
	 * @param 	dataBean      	 	データBeanクラス
	 * @throws JOFGyomuException	業務アプリで致命的エラー(異常)を検知した場合
	 */
	public void insertSLQ2210(L.apl.web.JOAL220101Bean dataBean) throws JOFGyomuException {

		int iIndex = 0;
		DefaultListModel defModelSudeniData = (DefaultListModel) dataBean.getLstTaiyou();
		HashMap map = new HashMap();
		for (int count = 0; count < defModelSudeniData.getSize(); count++) {
			JOAL22010101Bean oJOAL22010101 = (JOAL22010101Bean) defModelSudeniData.getElementAt(count);

			boolean countFlg = insertIntoSingleSLQ2210(dataBean, oJOAL22010101, iIndex + 1, map);
			if(countFlg == true) {
				iIndex++;
			}
		}
	}

	/**
	 * ＬＴアウトバウンド興行抽出登録処理を行います。
	 * @param 	dataBean      	 	データBeanクラス
	 * @throws JOFGyomuException	業務アプリで致命的エラー(異常)を検知した場合
	 */
	public void insertSLQ2220(L.apl.web.JOAL220101Bean dataBean) throws JOFGyomuException {

		DefaultListModel defModelSudeniData = (DefaultListModel) dataBean.getLstTyusyutuKekka();
		for (int count = 0; count < defModelSudeniData.getSize(); count++) {
			JOAL22010102Bean oJOAL22010102 = (JOAL22010102Bean) defModelSudeniData.getElementAt(count);
			insertIntoSingleSLQ2220(dataBean, oJOAL22010102, count + 1);
		}
	}

	/**
	 * ＬＴアウトバウンド送信内容登録処理を行います。
	 * @param 	dataBean      	 	データBeanクラス
	 * @throws JOFGyomuException	業務アプリで致命的エラー(異常)を検知した場合
	 */
	public void insertSLQPC2240(L.apl.web.JOAL220101Bean dataBean) throws JOFGyomuException {

		JOFDBAccesser dbAccessor = null;
		ArrayList conSLQ2240 = null;
		Clob clobObj = null;
		ArrayList conInboundSLQ = new ArrayList();

		//DBアクセス
		dbAccessor = new JOFDBAccesser();
		conSLQ2240 = new ArrayList(11);
		conSLQ2240.add(dataBean.getStrOutBndID().getText());
		conSLQ2240.add(pc);
		conSLQ2240.add(dataBean.getStrKenmei().getText());
		clobObj = JCFDBUtil.stringToClob(dataBean.getStrSousinNaiyou().getText());
		conSLQ2240.add(clobObj);
		ComboBox cmbReplyTo = dataBean.getCmbReplyTo();
		conSLQ2240.add(cmbReplyTo.getTextAt(cmbReplyTo.getSelectedIndex()));
		conSLQ2240.add(JOFUtil.getRegUserID());
		conSLQ2240.add(CLASS_NAME);
		conSLQ2240.add(JOFUtil.getConsoleID());
		conSLQ2240.add(JOFUtil.getRegUserID());
		conSLQ2240.add(CLASS_NAME);
		conSLQ2240.add(JOFUtil.getConsoleID());
		try {
			dbAccessor.execute("SLQ2240", conSLQ2240);
		} finally {
			dbAccessor.close();
		}
	}

	/**
	 * ＬＴアウトバウンド送信内容(携帯)登録の処理を行います。
	 * @param 	dataBean      	 	データBeanクラス
	 * @throws JOFGyomuException	業務アプリで致命的エラー(異常)を検知した場合
	 */
	public void insertSLQKeitai2240(L.apl.web.JOAL220101Bean dataBean) throws JOFGyomuException {

		JOFDBAccesser dbAccessor = null;
		ArrayList conSLQ2240 = null;
		Clob clobObj = null;

		//DBアクセス
		dbAccessor= new JOFDBAccesser();
		conSLQ2240 = new ArrayList(11);
		conSLQ2240.add(dataBean.getStrOutBndID().getText());
		conSLQ2240.add(keitai);
		conSLQ2240.add(dataBean.getStrKenmeiKeitai().getText());
		clobObj = JCFDBUtil.stringToClob(dataBean.getStrSousinNaiyokeitai().getText());
		conSLQ2240.add(clobObj);
		ComboBox cmbReplyTo = dataBean.getCmbReplyTo();
		conSLQ2240.add(cmbReplyTo.getTextAt(cmbReplyTo.getSelectedIndex()));
		conSLQ2240.add(JOFUtil.getRegUserID());
		conSLQ2240.add(CLASS_NAME);
		conSLQ2240.add(JOFUtil.getConsoleID());
		conSLQ2240.add(JOFUtil.getRegUserID());
		conSLQ2240.add(CLASS_NAME);
		conSLQ2240.add(JOFUtil.getConsoleID());
		try {
			dbAccessor.execute("SLQ2240", conSLQ2240);
		} finally {
			dbAccessor.close();
		}
	}

	/**
	 * ＬＴアウトバウンド管理の更新処理を行います。
	 * @param 	dataBean      	 	データBeanクラス
	 * @throws JOFGyomuException	業務アプリで致命的エラー(異常)を検知した場合
	 */
	public void updateSLQ2320(L.apl.web.JOAL220101Bean dataBean) throws JOFGyomuException {

		JOFDBAccesser dbAccesserSLQ2320 = null;
		ArrayList	conSLQ2320	= null;
		String		strRiyousya	= null;

		try {
			//利用者名称の取得
			strRiyousya = getRiyousyaName();
		}catch(Exception e){
			return;
		}

		//DBアクセス
		dbAccesserSLQ2320 = new JOFDBAccesser();

		conSLQ2320 = new ArrayList(22); //優先配信フラグを追加

		conSLQ2320.add(getYouyinID(dataBean));
		Date now = new Date();
		SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");

		sf.setLenient(false);
		String curDate = sf.format(now);

		conSLQ2320.add(curDate.substring(0, 8));
		conSLQ2320.add(dataBean.getStrHaisinkibou().replaceAll("/", "").trim());
		conSLQ2320.add(dataBean.getCmbTemplateKensaku().getValueAt(dataBean.getCmbTemplateKensaku().getSelectedIndex()));
		conSLQ2320.add(JCMClock.getBussinessDateTime());
		conSLQ2320.add(JOFUtil.getRegUserID());
		conSLQ2320.add(strRiyousya);

		//メモ欄
		conSLQ2320.add(dataBean.getStrMemo().getText());
		//払戻有無フラグ
		if(dataBean.getChkHaraimodoshiUmu().getChecked()){
			conSLQ2320.add("1");
		}else{
			conSLQ2320.add("0");
		}
		//払戻区分(ラジオ釦)
		int uKubun = dataBean.getButlHaraimodoshiInfo().getSelectedIndex();
		if(uKubun == -1){
			conSLQ2320.add("0");
		}else{
			String hUriKubun = dataBean.getButlHaraimodoshiInfo().getValueAt(dataBean.getButlHaraimodoshiInfo().getSelectedIndex());
			JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "☆払戻区分:" + hUriKubun);
			conSLQ2320.add(hUriKubun);
		}
		//再販売＿売り止め区分(ラジオ釦)
		int hKubun = dataBean.getButlHaraimodoshiNaiyo().getSelectedIndex();
		if(hKubun == -1){
			conSLQ2320.add("0");
		}else{
			String hBackKubun = dataBean.getButlHaraimodoshiNaiyo().getValueAt(dataBean.getButlHaraimodoshiNaiyo().getSelectedIndex());
			JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "☆売止区分:" + hBackKubun);
			conSLQ2320.add(hBackKubun);
		}
		//エラー戻りOB要フラグ
		if(dataBean.getChkErrBack().getChecked()){
			conSLQ2320.add("1");
		}else{
			conSLQ2320.add("0");
		}
		//原稿チェック有無フラグ
		if(dataBean.getChkGenkouCheck().getChecked()){
			conSLQ2320.add("1");
		}else{
			conSLQ2320.add("0");
		}
		//購入者抽出有フラグ
		if(dataBean.getChkBuyerTyusyutsu().getChecked()){
			conSLQ2320.add("1");
		}else{
			conSLQ2320.add("0");
		}
		//返信要フラグ
		if(dataBean.getChkHenshinUmu().getChecked()){
			conSLQ2320.add("1");
		}else{
			conSLQ2320.add("0");
		}
		//返信日
		conSLQ2320.add(dataBean.getStrHenshinbi().replaceAll("/","").trim());
		//返信内容
		conSLQ2320.add(dataBean.getStrHenshinNaiyo().getText());

		conSLQ2320.add(JOFUtil.getRegUserID());
		conSLQ2320.add(CLASS_NAME);
		conSLQ2320.add(JOFUtil.getConsoleID());

		//優先配信フラグ
		if(dataBean.getChkHaishinYusen().getChecked()){
			conSLQ2320.add("1");
		}else{
			conSLQ2320.add("0");
		}

		conSLQ2320.add(dataBean.getStrOutBndID().getText());
		try {
			dbAccesserSLQ2320.execute("SLQ2320", conSLQ2320);
		} finally {
			dbAccesserSLQ2320.close();
		}
	}

	/**
	 * ＬＴアウトバウンド対象者の更新処理を行います。
	 * @param 	dataBean      	 	データBeanクラス
	 * @throws JOFGyomuException	業務アプリで致命的エラー(異常)を検知した場合
	 */
	public void updateSLQ2270(L.apl.web.JOAL220101Bean dataBean) throws JOFGyomuException {

		JOFDBAccesser dbAccesserSLQ2270 = null;
		JCMLQToiawaseRegistry toiawaseCommon = new JCMLQToiawaseRegistry();

		int retNum = 0;
		int curNum = 1;

		dbAccesserSLQ2270 = new JOFDBAccesser();

		DefaultListModel defModelSudeniData = (DefaultListModel) dataBean.getLstTaiyou();

		HashMap map = new HashMap();
		for (int count = 0; count < defModelSudeniData.getSize(); count++) {
			JOAL22010101Bean oJOAL22010101 = (JOAL22010101Bean) defModelSudeniData.getElementAt(count);
			ArrayList conSLQ2270 = new ArrayList();
			conSLQ2270.add(oJOAL22010101.getStrfileSoshikiCD());
			conSLQ2270.add(oJOAL22010101.getStrfileKainID());
			conSLQ2270.add(oJOAL22010101.getStrfileSimei());
			conSLQ2270.add(oJOAL22010101.getStrfileMeimei());
			conSLQ2270.add(oJOAL22010101.getStrfileKanaSimei());
			conSLQ2270.add(oJOAL22010101.getStrfileKanaMeimei());
			conSLQ2270.add(oJOAL22010101.getStrfilePcAddress());
			conSLQ2270.add(oJOAL22010101.getStrfileTelNo());
			String PC_KeitaiKubun = toiawaseCommon.getPcKeitaiKbn(oJOAL22010101.getStrfilePcAddress());
			conSLQ2270.add(PC_KeitaiKubun);
			conSLQ2270.add(JOFUtil.getRegUserID());
			conSLQ2270.add(CLASS_NAME);
			conSLQ2270.add(JOFUtil.getConsoleID());
			conSLQ2270.add(oJOAL22010101.getStrfileOutBndID());
			conSLQ2270.add(oJOAL22010101.getStrTaisyouRenban());

			try {
				retNum = dbAccesserSLQ2270.execute("SLQ2270", conSLQ2270);
				if (retNum == 0) {
					boolean countFlg = insertIntoSingleSLQ2210(dataBean, oJOAL22010101, curNum, map);
					if(countFlg == true) {
						curNum++;
					}
				}
			} finally {
				dbAccesserSLQ2270.close();
			}
		}
	}

	/**
	 * アウトバウンド対象者TBLデータ存在チェック
	 * @param 	strOutBndID     	アウトバウンドID
	 * @param 	strPcKtKubun  		ＰＣ＿携帯＿ＴＥＬ区分
	 * @return boolean				true OR false
	 * @throws JOFGyomuException	業務アプリで致命的エラー(異常)を検知した場合
	 * @throws SQLException		DBアクセス処理で致命的エラー(異常)を検知した場合
	 */
	public boolean getCntTaisyousyaData(String strOutBndID, String strPcKtKubun) throws JOFGyomuException, SQLException {

		JOFDBAccesser dbAccesserSLQ4485 = null;
		ArrayList conSLQ4485 = null;
		ResultSet rsSLQ4485 = null;
		boolean dataCnt = false;

		//DBアクセス
		dbAccesserSLQ4485 = new JOFDBAccesser();
		conSLQ4485 = new ArrayList(2);
		conSLQ4485.add(strOutBndID);
		conSLQ4485.add(strPcKtKubun);

		try {
			rsSLQ4485 = dbAccesserSLQ4485.getResultSetBySelect("SLQ4485", conSLQ4485);

			while (rsSLQ4485.next()) {
				dataCnt = true;
			}
		} finally {
			dbAccesserSLQ4485.close();
		}
		return dataCnt;
	}

	/**
	 * ＬＴアウトバウンド対象者の登録処理を行います。
	 * @param 	dataBean      	 	データBeanクラス
	 * @param 	oJOAL22010101		JOAL22010101Bean
	 * @param  count				アウトバウンド連番
	 * @throws JOFGyomuException	業務アプリで致命的エラー(異常)を検知した場合
	 */
	public boolean insertIntoSingleSLQ2210(L.apl.web.JOAL220101Bean dataBean, JOAL22010101Bean oJOAL22010101, int count, HashMap map) throws JOFGyomuException {

		JOFDBAccesser dbAccesserSLQ2210 = null;
		JCMLQToiawaseRegistry toiawaseCommon = new JCMLQToiawaseRegistry();

		dbAccesserSLQ2210 = new JOFDBAccesser();
		ArrayList conSLQ2210 = new ArrayList(16);

		String PC_KeitaiKubun = "0";
		String wkTelNo = "0";
		conSLQ2210.add(JOFUtil.getConsoleID());
		try {
			dbAccesserSLQ2210.execute("SLQ9390", conSLQ2210);
		} finally {
			dbAccesserSLQ2210.close();
		}

		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "★対象者登録：興行コード : " + oJOAL22010101.getStrGyoukouCode());
		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "★対象者登録：興行サブコード : " + oJOAL22010101.getStrSubGyoukouCode());
		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "★対象者登録：受付情報名 : " + oJOAL22010101.getStrUketukeJohoName());
		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "★対象者登録：受付情報コード : " + oJOAL22010101.getStrUketukeJohoCode());
		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "★対象者登録：興行名 : " + oJOAL22010101.getStrGyoukoumei());
		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "★対象者登録：公演コード : " + oJOAL22010101.getStrKouenCode());
		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "★対象者登録：公演名 : " + oJOAL22010101.getStrKouenmei());
		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "★対象者登録：公演日 : " + oJOAL22010101.getStrKouenhi());
		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "★対象者登録：開演 : " + oJOAL22010101.getStrKaien());
		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "★対象者登録：オーダーステータス : " + oJOAL22010101.getStrOrderStatus());
		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "★対象者登録：オーダーステータス名 : " + oJOAL22010101.getStrOrderName());
		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "★対象者登録：興行インデックス : " + oJOAL22010101.getStrOutBaundKougyoIndex());

		return true;
	}

	/**
	 * ＬＴアウトバウンド興行抽出の更新処理を行います。
	 * @param 	dataBean      	 	データBeanクラス
	 * @throws JOFGyomuException	業務アプリで致命的エラー(異常)を検知した場合
	 */
	public void updateSLQ2290(L.apl.web.JOAL220101Bean dataBean) throws JOFGyomuException {

		JCMCheckUtil oJCMCheckUtil = new JCMCheckUtil();

		JOFDBAccesser dbAccesserSLQ2290 = null;
		int retNum = 0;
		int curNum = 1;

		dbAccesserSLQ2290 = new JOFDBAccesser();

		DefaultListModel defModelSudeniData = (DefaultListModel) dataBean.getLstTyusyutuKekka();

		for (int count = 0; count < defModelSudeniData.getSize(); count++) {
			JOAL22010102Bean oJOAL22010102 = (JOAL22010102Bean) defModelSudeniData.getElementAt(count);
			ArrayList conSLQ2290 = new ArrayList();
			conSLQ2290.add(oJOAL22010102.getStrGyoukouCode());
			conSLQ2290.add(oJOAL22010102.getStrSubGyoukouCode());
			conSLQ2290.add(oJOAL22010102.getStrUketukeJohoCode());
			conSLQ2290.add(oJOAL22010102.getStrGyoukoumei());
			conSLQ2290.add(oJOAL22010102.getStrKouenCode());
			String strkaien = oJOAL22010102.getStrKouenhi().replaceAll("/", "");
			conSLQ2290.add(strkaien);
			conSLQ2290.add(oJOAL22010102.getStrKaien());
			conSLQ2290.add(oJOAL22010102.getStrOrderStatus());
			conSLQ2290.add(JOFUtil.getRegUserID());
			conSLQ2290.add(CLASS_NAME);
			conSLQ2290.add(JOFUtil.getConsoleID());
			conSLQ2290.add(oJOAL22010102.getStrAutoBoundID());
			conSLQ2290.add(oJOAL22010102.getStrTyuusyutuRenban());

			try {
				retNum = dbAccesserSLQ2290.execute("SLQ2290", conSLQ2290);
			} finally {
				dbAccesserSLQ2290.close();
			}
		}
	}

	/**
	 * ＬＴアウトバウンド興行抽出の登録処理を行います。
	 * @param 	dataBean      	 	データBeanクラス
	 * @param 	oJOAL22010102		JOAL22010102Bean
	 * @param  count			 	アウトバウンド連番
	 * @throws JOFGyomuException	業務アプリで致命的エラー(異常)を検知した場合
	 */
	public void insertIntoSingleSLQ2220(L.apl.web.JOAL220101Bean dataBean, JOAL22010102Bean oJOAL22010102, int count) throws JOFGyomuException {

		JOFDBAccesser dbAccesserSLQ2220 = null;

		dbAccesserSLQ2220 = new JOFDBAccesser();
		JCMCheckUtil oJCMCheckUtil = new JCMCheckUtil();
		ArrayList conSLQ2220 = new ArrayList(16);
		HashMap map = new HashMap();
		map = dataBean.getOutBaundKougyouMap();
		String strIndex = getOutBoundKougyouIndex(map,
												  oJOAL22010102.getStrGyoukouCode(),
												  oJOAL22010102.getStrSubGyoukouCode(),
												  oJOAL22010102.getStrUketukeJohoCode(),
												  oJOAL22010102.getStrGyoukoumei(),
												  oJOAL22010102.getStrKouenCode(),
												  oJOAL22010102.getStrKouenhi(),
												  oJOAL22010102.getStrKaien(),
												  oJOAL22010102.getStrOrderStatus());
		conSLQ2220.add(dataBean.getStrhidTaisyouAutoboundID());
		conSLQ2220.add(strIndex);
		conSLQ2220.add(oJOAL22010102.getStrGyoukouCode());
		conSLQ2220.add(oJOAL22010102.getStrSubGyoukouCode());
		conSLQ2220.add(oJOAL22010102.getStrUketukeJohoCode());
		conSLQ2220.add(oJOAL22010102.getStrGyoukoumei());
		conSLQ2220.add(oJOAL22010102.getStrKouenCode());
		conSLQ2220.add(oJOAL22010102.getStrKouenhi().replaceAll("/", ""));

		if (!oJCMCheckUtil.isCheckExist(oJOAL22010102.getStrKaien())) {
			conSLQ2220.add("");
		} else {
			conSLQ2220.add(oJOAL22010102.getStrKaien());
		}
		conSLQ2220.add(oJOAL22010102.getStrOrderStatus());
		conSLQ2220.add(JOFUtil.getRegUserID());
		conSLQ2220.add(CLASS_NAME);
		conSLQ2220.add(JOFUtil.getConsoleID());
		conSLQ2220.add(JOFUtil.getRegUserID());
		conSLQ2220.add(CLASS_NAME);
		conSLQ2220.add(JOFUtil.getConsoleID());

		try {
			dbAccesserSLQ2220.execute("SLQ2220", conSLQ2220);

		} finally {
			dbAccesserSLQ2220.close();
		}

	}

	/**
	 * オーダー明細情報を取得する処理を行います。
	 * @param 	dataBean      	 	データBeanクラス
	 * @return ArrayList			オーダー明細情報
	 * @throws JOFGyomuException	業務アプリで致命的エラー(異常)を検知した場合
	 * @throws SQLException		DBアクセス処理で致命的エラー(異常)を検知した場合
	 */
	public ArrayList getOrderMeisyouInfo(JOAL220101Bean dataBean) throws JOFGyomuException, SQLException {

		ArrayList retKanriBango = null;

		String strComboBox = "1";
		String strListBox = "0";

		JOFDBAccesser dbAccesserSLQ2090 = null;
		dbAccesserSLQ2090 = new JOFDBAccesser();

		ArrayList conSLQ2090 = new ArrayList();
		ResultSet rsSLQ2090 = null;
		HashMap wkMap = new HashMap();
		String strWhereCondition = "";

		conSLQ2090.add(dataBean.getStrKyoukouCode().getText());

		//受付情報コード
		if (strComboBox.equals(dataBean.getStrUketukeFlg())) {
			if (dataBean.getCmbWuketukeJyouhou().getSelectedIndex() > 0) {
				strWhereCondition = " A.\"受付情報コード\" IN (?) ";
				conSLQ2090.add(dataBean.getCmbWuketukeJyouhou().getValueAt(dataBean.getCmbWuketukeJyouhou().getSelectedIndex()));
			}
		} else {
			ListBox lstUketuke = dataBean.getLstUketuke();
			int[] intUketuke = lstUketuke.getSelectedIndexes();
			if (intUketuke != null) {
				strWhereCondition = " A.\"受付情報コード\" IN (";
				for (int count = 0; count < intUketuke.length; count++) {
					strWhereCondition = strWhereCondition + "?,";
					conSLQ2090.add(lstUketuke.getValueAt(intUketuke[count]));
				}
				strWhereCondition = strWhereCondition.substring(0, strWhereCondition.length() - 1) + ") ";
			}
		}
		//オーダー明細ステータス
		if (strComboBox.equals(dataBean.getStrOrderStatusFlg())) {
			if (dataBean.getCmbOrdersutetansu().getSelectedIndex() > 0) {
				strWhereCondition = strWhereCondition + " AND A.\"オーダー明細ステータス\" IN (?) ";
				conSLQ2090.add(dataBean.getCmbOrdersutetansu().getValueAt(dataBean.getCmbOrdersutetansu().getSelectedIndex()));
			}
		} else {
			ListBox lstOrderStatus = dataBean.getLstOrderStatus();
			int[] intOrderStatus = lstOrderStatus.getSelectedIndexes();
			if (intOrderStatus != null) {
				strWhereCondition = strWhereCondition + " AND A.\"オーダー明細ステータス\" IN (";
				for (int count = 0; count < intOrderStatus.length; count++) {
					strWhereCondition = strWhereCondition + "?,";
					conSLQ2090.add(lstOrderStatus.getValueAt(intOrderStatus[count]));
				}
				strWhereCondition = strWhereCondition.substring(0, strWhereCondition.length() - 1) + ") ";
			}
		}

		if (strComboBox.equals(dataBean.getStrKouenhiFlg())) {
			if (dataBean.getCmbKouenbi().getSelectedIndex() > 0) {
				strWhereCondition = strWhereCondition + " AND F.\"公演日\" IN (?) ";
				conSLQ2090.add(dataBean.getCmbKouenbi().getValueAt(dataBean.getCmbKouenbi().getSelectedIndex()).trim());
			}
		} else {
			ListBox lstKouen = dataBean.getLstKouenhi();
			int[] intKouen = lstKouen.getSelectedIndexes();
			if (intKouen != null) {
				strWhereCondition = strWhereCondition + " AND F.\"公演日\" IN (";
				for (int count = 0; count < intKouen.length; count++) {
					strWhereCondition = strWhereCondition + "?,";
					conSLQ2090.add(lstKouen.getValueAt(intKouen[count]).trim());
				}
				strWhereCondition = strWhereCondition.substring(0, strWhereCondition.length() - 1) + ") ";
			}
		}

		// 興行名
		if (strComboBox.equals(dataBean.getStrKouenmeiFlg())) {
			if (dataBean.getCmbKouenmei().getSelectedIndex() > 0) {
				strWhereCondition = strWhereCondition + " AND A.\"興行サブコード\" IN (?) ";
				conSLQ2090.add(dataBean.getCmbKouenmei().getValueAt(dataBean.getCmbKouenmei().getSelectedIndex()).trim());
			}
		} else {
			ListBox lstKouen = dataBean.getLstKouenmei();
			int[] intKouen = lstKouen.getSelectedIndexes();
			if (intKouen != null) {
				strWhereCondition = strWhereCondition + " AND A.\"興行サブコード\" IN (";
				for (int count = 0; count < intKouen.length; count++) {
					strWhereCondition = strWhereCondition + "?,";
					conSLQ2090.add(lstKouen.getValueAt(intKouen[count]).trim());
				}
				strWhereCondition = strWhereCondition.substring(0, strWhereCondition.length() - 1) + ") ";
			}
		}

		//公演時間のチェック
		String strKouenJikan = dataBean.getStrKaienjikan().getText();
		String strHour = null;
		String strMinute = null;
		JCMCheckUtil oJCMCheckUtil = new JCMCheckUtil();
		if (oJCMCheckUtil.isCheckExist(strKouenJikan)) {
			StringTokenizer sTokenizer = new StringTokenizer(strKouenJikan, ":");
			strHour = sTokenizer.nextToken();
			strMinute = sTokenizer.nextToken();
			strWhereCondition = strWhereCondition + " AND F.\"開演時間１時\" IN (?) ";
			conSLQ2090.add(strHour);
			strWhereCondition = strWhereCondition + " AND F.\"開演時間１分\" IN (?) ";
			conSLQ2090.add(strMinute);
		}

		strWhereCondition = strWhereCondition + " ORDER BY D.\"組織コード\",D.\"会員ＩＤ\"";

		try {
			rsSLQ2090 = dbAccesserSLQ2090.getResultSetBySelect("SLQ2090", conSLQ2090, strWhereCondition);
			int intRecordNO = 0;
			while (rsSLQ2090.next()) {
				if (retKanriBango == null) {
					retKanriBango = new ArrayList();
				}
				JOAL22010101Bean oJOAL22010101Bean = new JOAL22010101Bean();
				//管理番号
				oJOAL22010101Bean.setStrKanriNO(rsSLQ2090.getString(1));
				//興行コード
				oJOAL22010101Bean.setStrGyoukouCode(rsSLQ2090.getString(3));
				//サプ興行コード
				oJOAL22010101Bean.setStrSubGyoukouCode(rsSLQ2090.getString(4));
				//受付情報コード
				oJOAL22010101Bean.setStrUketukeJohoCode(rsSLQ2090.getString(5));
				//オーダステータス
				oJOAL22010101Bean.setStrOrderStatus(rsSLQ2090.getString(6));
				//受付情報名
				oJOAL22010101Bean.setStrUketukeJohoName(rsSLQ2090.getString(7));
				//興行名称
				oJOAL22010101Bean.setStrGyoukoumei(rsSLQ2090.getString(8));
				//受付チャネル
				oJOAL22010101Bean.setStrWuketuketyaneru(rsSLQ2090.getString(9));
				//一見・会員区分
				oJOAL22010101Bean.setStrIsKaiyi(rsSLQ2090.getString(10));
				//組織コード
				oJOAL22010101Bean.setStrSosikiID(rsSLQ2090.getString(11));
				//会員ＩＤ
				oJOAL22010101Bean.setStrKaiyinID(rsSLQ2090.getString(12));
				//組織種別
				oJOAL22010101Bean.setStrOrgSyubetu(rsSLQ2090.getString(13));
				//公演コード
				oJOAL22010101Bean.setStrKouenCode(rsSLQ2090.getString(14));
				//公演日
				oJOAL22010101Bean.setStrKouenhi(rsSLQ2090.getString(16));
				//オーダステータス名
				oJOAL22010101Bean.setStrOrderName(rsSLQ2090.getString(17));

				String sTime = "";
 				try{
  				strHour = rsSLQ2090.getString(18);
				strMinute = rsSLQ2090.getString(19);
 				}catch(SQLException e)
 				{}
 				if(strHour!=null && strHour.length()>0)
				{
					sTime = strHour +":"+strMinute;
 	 				oJOAL22010101Bean.setStrKaien(sTime);
				}

				// アウトバウンド興行情報単位でのインデックスを作成します。
				oJOAL22010101Bean.setStrOutBaundKougyoIndex(setOutBoundKougyouIndex(wkMap,
														  oJOAL22010101Bean.getStrGyoukouCode(),
														  oJOAL22010101Bean.getStrSubGyoukouCode(),
														  oJOAL22010101Bean.getStrUketukeJohoCode(),
														  oJOAL22010101Bean.getStrGyoukoumei(),
														  oJOAL22010101Bean.getStrKouenCode(),
														  oJOAL22010101Bean.getStrKouenhi(),
														  oJOAL22010101Bean.getStrKaien(),
														  oJOAL22010101Bean.getStrOrderStatus()));
				dataBean.setOutBaundKougyouMap(wkMap);
				retKanriBango.add(intRecordNO, oJOAL22010101Bean);
				intRecordNO++;
			}
		} finally {
			dbAccesserSLQ2090.close();
		}
		return retKanriBango;
	}

	/**
	 * Concreteユーザー情報を取得する処理を行います。
	 * @param 	lstUserInfo			リストユーザー情報
	 * @return ArrayList			Concreteユーザー情報
	 * @throws JOFGyomuException	業務アプリで致命的エラー(異常)を検知した場合
	 * @throws SQLException		DBアクセス処理で致命的エラー(異常)を検知した場合
	 */
	public ArrayList getConcreteUserInfo(JOAL220101Bean dataBean, ArrayList lstUserInfo) throws JOFGyomuException, SQLException {

		ArrayList retLstInfo = new ArrayList(lstUserInfo.size() * 2);
		JCMCheckUtil chk = new JCMCheckUtil();
		JOAL22010101Bean wkBean = new JOAL22010101Bean();
		int intCount = 0;


		// 取得したオーダー情報分処理を行う
		for (int count = 0; count < lstUserInfo.size(); count++) {

			// 全チャネルチェック取得（画面より）
			boolean chkAllChanel = dataBean.getChkSubetetyaneruTaisyou().getChecked();

			wkBean = (JOAL22010101Bean) lstUserInfo.get(count);	// リスト->Beanへキャスト
			String kaiinKbn = wkBean.getStrIsKaiyi();			// 会員／一見区分の取得

			// 会員／一見区分が1:会員の場合
			if (KAIIN_KBN_KAIIN.equals(kaiinKbn)) {

				String strSyubetu = wkBean.getStrOrgSyubetu();	// 組織種別の取得
				String kaiinID = wkBean.getStrKaiyinID();		// 会員ＩＤの取得
				String soshikiCd = wkBean.getStrSosikiID();		// 組織ＩＤの取得

				// 組織種別が、1：ｅ＋組織、2：ｅ＋共有組織　の場合
				if (SOSHIKI_KIND_EPLUS.equals(strSyubetu) || SOSHIKI_KIND_ECOMMON.equals(strSyubetu)) {

					// ｅ＋会員情報の取得
					wkBean = getEPlusInfo(wkBean, soshikiCd, kaiinID, "SLQ0110");

				// 組織種別が、3：他組織提携ＡＳＰ組織、4：他組織完全ＡＳＰ組織　の場合、
				} else if (SOSHIKI_KIND_TEIKEIASP.equals(strSyubetu) || SOSHIKI_KIND_KANZENASP.equals(strSyubetu)) {

					// 他組織情報の取得
					wkBean = getEPlusInfo(wkBean, soshikiCd, kaiinID, "SLQ1170");

				}
			// 会員／一見区分が"2:一見"の場合
			} else if (KAIIN_KBN_ICHIGEN.equals(kaiinKbn)) {

				// 一見購入者情報の取得
				wkBean = getSonotaInfo(wkBean, wkBean.getStrKanriNO());

				// 一見の時はチェックありに設定する
				chkAllChanel = true;
			}

			// 全チャネルにチェックあり
			if(chkAllChanel == true){
				// 取得した顧客情報をリストに設定
				retLstInfo = setMemberInfo(retLstInfo, wkBean, 1, intCount);
				intCount = retLstInfo.size();

			// 全チャネルにチェックなし
			} else {
				// 送信要否フラグをチェック
				int iChkFlg = chkMailSendFlg(wkBean.getStrPcMailSendFlg(), wkBean.getStrKeitaiMailSendFlg());
				// 取得した顧客情報をリストに設定
				retLstInfo = setMemberInfo(retLstInfo, wkBean, iChkFlg, intCount);
				intCount = retLstInfo.size();
			}
		}
		for(int i=0; i< retLstInfo.size(); i++) {
			JOAL22010101Bean wk = (JOAL22010101Bean)retLstInfo.get(i);
			JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "リスト情報 " + i + "番目 " + ": " + wk.getStrAddress());

			JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "リスト情報(興行インデックス) " + i + "番目 " + ": " + wk.getStrOutBaundKougyoIndex());
		}
		return retLstInfo;
	}

	/**
	 * EPlus情報を取得する処理を行います。
	 * @param 	oJOAL22010101Bean   JOAL22010101Bean
	 * @return JOAL22010101Bean	EPlus情報
	 * @throws JOFGyomuException	業務アプリで致命的エラー(異常)を検知した場合
	 * @throws SQLException		DBアクセス処理で致命的エラー(異常)を検知した場合
	 */
	public JOAL22010101Bean getEPlusInfo(JOAL22010101Bean setBean, String soshiki, String kaiinId, String sqlId) throws JOFGyomuException, SQLException {

		JCMCheckUtil chk = new JCMCheckUtil();				// チェッククラスの生成
		JOFDBAccesser dba = null;
		dba = new JOFDBAccesser();							// DBアクセッサの生成
		ArrayList cond = new ArrayList(2);					// SQL条件用リスト
		ArrayList ret = new ArrayList();					// SQL取得用リスト

		// SQL条件の設定
		cond.add(soshiki);									// 組織コードの設定　（条件）　
		cond.add(kaiinId);									// 会員ＩＤの設定　　（条件）
		try {

			JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, sqlId + " (条件) : " + cond);
			ret = dba.select(sqlId, cond);
			JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, sqlId + " (結果) : " + ret);

			if(ret.size() > 0) {
				HashMap map = (HashMap)ret.get(0);
				setBean.setStrSimei(map.get("漢字氏名＿姓") == null ? "" : map.get("漢字氏名＿姓").toString());
				setBean.setStrMeimei(map.get("漢字氏名＿名") == null ? "" : map.get("漢字氏名＿名").toString());
				setBean.setStrKanaSimei(map.get("カナ氏名＿姓") == null ? "" : map.get("カナ氏名＿姓").toString());
				setBean.setStrKanaMeimei(map.get("カナ氏名＿名") == null ? "" : map.get("カナ氏名＿名").toString());
				String wkTelNo = map.get("電話番号") == null ? "" : map.get("電話番号").toString();
				if(chk.isCheckExist(wkTelNo)) {
					setBean.setStrRenrakuDenwabango(map.get("電話番号") == null ? "" : map.get("電話番号").toString());
				} else {
					setBean.setStrRenrakuDenwabango(map.get("携帯電話番号") == null ? "" : map.get("携帯電話番号").toString());
				}
				setBean.setStrPcMailAddress(map.get("ＰＣメールアドレス") == null ? "" : map.get("ＰＣメールアドレス").toString());
				setBean.setStrKeitaiMailAddress(map.get("携帯メールアドレス") == null ? "" : map.get("携帯メールアドレス").toString());
				setBean.setStrPcMailSendFlg(map.get("ＰＣメールアドレス送信要否") == null ? "" : map.get("ＰＣメールアドレス送信要否").toString());
				setBean.setStrKeitaiMailSendFlg(map.get("携帯メールアドレス送信要否") == null ? "" : map.get("携帯メールアドレス送信要否").toString());
			}
		} finally {
			dba.close();
		}

		return setBean;
	}


	/**
	 * その他情報を取得する処理を行います。
	 * @param 	oJOAL22010101Bean   JOAL22010101Bean
	 * @return oJOAL22010101Bean	その他情報
	 * @throws JOFGyomuException	業務アプリで致命的エラー(異常)を検知した場合
	 * @throws SQLException		DBアクセス処理で致命的エラー(異常)を検知した場合
	 */
	public JOAL22010101Bean getSonotaInfo(JOAL22010101Bean setBean, String kanriNo) throws JOFGyomuException, SQLException {

		JOFDBAccesser dba = null;
		dba = new JOFDBAccesser();					// DBアクセッサの生成
		JCMCheckUtil util = new JCMCheckUtil();	// チェック部品の生成
		ArrayList cond = new ArrayList(1);			// SQL条件リストの生成
		ArrayList ret = new ArrayList();			// SQL取得リストの生成

		// SQL条件の設定
		cond.add(kanriNo);		// 管理番号の設定（条件）

		try {
			ret = dba.select("SLQ2103", cond);
			if(ret.size() > 0) {
				HashMap map = (HashMap)ret.get(0);
				setBean.setStrSimei(map.get("漢字氏名（姓）") == null ? "" : map.get("漢字氏名（姓）").toString());
				setBean.setStrMeimei(map.get("漢字氏名（名）") == null ? "" : map.get("漢字氏名（名）").toString());
				setBean.setStrKanaSimei(map.get("氏名姓（カナ）") == null ? "" : map.get("氏名姓（カナ）").toString());
				setBean.setStrKanaMeimei(map.get("氏名名（カナ）") == null ? "" : map.get("氏名名（カナ）").toString());
				setBean.setStrRenrakuDenwabango(map.get("電話番号") == null ? "" : map.get("電話番号").toString());
				setBean.setStrPcMailAddress(map.get("ＰＣメールアドレス") == null ? "" : map.get("ＰＣメールアドレス").toString());
				setBean.setStrKeitaiMailAddress(map.get("携帯メールアドレス") == null ? "" : map.get("携帯メールアドレス").toString());
			}

		} finally {
			dba.close();
		}
		return setBean;
	}

	/**
	 * 要因ＩＤを取得する処理を行います。
	 * @param 	dataBean	データBeanクラス
	 * @return String		要因ID
	 */
	public String getYouyinID(JOAL220101Bean dataBean) {

		//要因ＩＤを取得
		String retValue = null;

		if (dataBean.getCmbYouiin().getSelectedIndex() > 0) {
			retValue = dataBean.getCmbYouiin().getValueAt(dataBean.getCmbYouiin().getSelectedIndex()).trim();
		} else {
			retValue = dataBean.getStrAutoboundYouyinID();
		}
		return retValue;
	}

	/**
	 * 受付情報の存在チェックを行います。
	 * @param 	inGyoukouCode 		興行コード
	 * @param 	inSubCode 			興行コードサブ
	 * @return String[][]			受付情報
	 * @throws JOFGyomuException	業務アプリで致命的エラー(異常)を検知した場合
	 * @throws SQLException		DBアクセス処理で致命的エラー(異常)を検知した場合
	 */
	public String getUketukeJohoChk(String inGyoukouCode, String inSubCode) throws JOFGyomuException, SQLException {

		JOFDBAccesser dbAccesserSLQ2061 = null;
		ArrayList conSLQ2061 = null;
		ResultSet rsSLQ2061 = null;
		String strCount = "0";

		//DBアクセス
		dbAccesserSLQ2061 = new JOFDBAccesser();
		conSLQ2061 = new ArrayList();

		//検索条件
		conSLQ2061.add(inGyoukouCode);

		try {
			conSLQ2061.add(inSubCode);
			rsSLQ2061 = dbAccesserSLQ2061.getResultSetBySelect("SLQ2061", conSLQ2061);
			while (rsSLQ2061.next()) {
				strCount = rsSLQ2061.getString(1);
			}
		} finally {
			dbAccesserSLQ2061.close();
		}
		return strCount;
	}

	/**
	 * 利用者名称の取得処理。
	 * @return  strRiyousyaName     利用者名称
	 * @throws  JOFGyomuException   業務共通部品で致命的エラー(異常)を検知した場合
	 * @throws  SQLException        DBアクセス処理で致命的エラー(異常)を検知した場合
	 */
	public String getRiyousyaName() throws JOFGyomuException, SQLException {

		JOFDBAccesser dbAccesserSLQ4332 = null;
		ArrayList conSLQ4332 = null;
		ResultSet rsSLQ4332 = null;

		String strRiyousyaName = null;

		//DBアクセス
		dbAccesserSLQ4332 = new JOFDBAccesser();

		conSLQ4332 = new ArrayList(1);
		conSLQ4332.add(JOFUtil.getRegUserID());

		//利用者名称を取得する
		try {
			rsSLQ4332 = dbAccesserSLQ4332.getResultSetBySelect("SLQ4332", conSLQ4332);

			if (rsSLQ4332.next()) {
				strRiyousyaName = rsSLQ4332.getString(1);
			}
		} finally {
			dbAccesserSLQ4332.close();
		}

		return strRiyousyaName;
	}

	/**
	 * 利用者名称の取得処理。
	 * @return  strRiyousyaName     利用者名称
	 * @throws  JOFGyomuException   業務共通部品で致命的エラー(異常)を検知した場合
	 * @throws  SQLException        DBアクセス処理で致命的エラー(異常)を検知した場合
	 */
	public String getRiyousyaCodeToName(String risyousyaCode) throws JOFGyomuException, SQLException {

		JOFDBAccesser dbAccesserSLQ4332 = null;
		ArrayList conSLQ4332 = null;
		ResultSet rsSLQ4332 = null;

		String strRiyousyaName = null;

		//DBアクセス
		dbAccesserSLQ4332 = new JOFDBAccesser();

		conSLQ4332 = new ArrayList(1);
		conSLQ4332.add(risyousyaCode);

		//利用者名称を取得する
		try {
			rsSLQ4332 = dbAccesserSLQ4332.getResultSetBySelect("SLQ4332", conSLQ4332);

			if (rsSLQ4332.next()) {
				strRiyousyaName = rsSLQ4332.getString(1);
			}
		} finally {
			dbAccesserSLQ4332.close();
		}

		return strRiyousyaName;
	}

	/**
	 * 登録者コードの取得処理。
	 *
	 * @param	strAutoBoundID		アウトバウンドＩＤ
	 * @return	String				登録者コード
	 * @throws	JOFGyomuException	業務共通部品で致命的エラー(異常)を検知した場合
	 * @throws	SQLException		DBアクセス処理で致命的エラー(異常)を検知した場合
	 */
	private String getOutBoundRegCode(String strAutoBoundID) throws JOFGyomuException, SQLException {

		JOFDBAccesser dba = new JOFDBAccesser();
		ArrayList con = new ArrayList();
		ArrayList ret = new ArrayList();

		String regCode = "";

		//DBアクセス
		con.add(strAutoBoundID);

		//利用者名称を取得する
		try {
			JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "条件（SLQ4531） : " + con);
			ret = dba.select("SLQ4531", con);
			JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "結果（SLQ4531） : " + ret);

			if((ret != null) && (ret.size() > 0)) {
				HashMap map = (HashMap)ret.get(0);
				regCode = map.get("登録担当者＿社員コード") == null ? "" : map.get("登録担当者＿社員コード").toString();
			}
		} finally {
			JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "登録者コード : " + regCode);
		}

		return regCode;
	}

	/**
	 * 送信メール上の置換文字のキーワードを取得する。
	 * @return  strKeyWord          置換文字のキーワード
	 */
	public ArrayList getRepKyeWord() throws JOFGyomuException, SQLException {
		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "getRepKyeWord str");
		ArrayList strKeyWord = new ArrayList(20);

		strKeyWord.add("\\$興行名\\$");								//  1
		strKeyWord.add("\\$公演名\\$");								//  2
		strKeyWord.add("\\$会場\\$");								//  3
		strKeyWord.add("\\$公演日\\$");								//  4
		strKeyWord.add("\\$開場時間\\$");							//  5
		strKeyWord.add("\\$開演時間\\$");							//  6
		strKeyWord.add("\\$発売日\\$");								//  7
		strKeyWord.add("\\$プレ（抽選系受付）受付期間（自）\\$");	//  8
		strKeyWord.add("\\$プレ（抽選系受付）受付期間（至）\\$");	//  9
		strKeyWord.add("\\$会員ＩＤ\\$");							// 10
		strKeyWord.add("\\$漢字氏名\\$");							// 11
		strKeyWord.add("\\$カナ氏名\\$");							// 12
		strKeyWord.add("\\$郵便番号\\$");							// 13
		strKeyWord.add("\\$登録住所\\$");							// 14
		strKeyWord.add("\\$電話番号\\$");							// 15
		strKeyWord.add("\\$携帯電話番号\\$"); 						// 16
		strKeyWord.add("\\$PCメールアドレス\\$"); 					// 17
		strKeyWord.add("\\$携帯メールアドレス\\$");					// 18
		strKeyWord.add("\\$組織\\$");								// 19
		strKeyWord.add("\\$登録決済方法\\$");						// 20

		return strKeyWord;

	}

	/**
	 * オンラインバッチ(アウトバウンドメール配信)を起動します
	 *
	 * @param	strBatchID			バッチID
	 * @param 	dataBean
	 */
	public void startOnlineBatch(String strBatchID, JOAL220101Bean dataBean) {

		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, " START!");

		// パラメータの作成
		String strParam1 = "P1=" + getAutoBoundID(dataBean);
		String strParam2 = "P2=" + "1"; //アウトバウンド旧or新バージョン（１）

		// バッチスタート
		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "***バッチ処理スタートします");
		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "***バッチ処理パラメータ１：" + strParam1);
		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "***バッチ処理パラメータ２：" + strParam2);
		JCMOnlineBatchStart jcmBatch = new JCMOnlineBatchStart();
		jcmBatch.setOnlineBatchJobID(strBatchID);
		jcmBatch.setParam(strParam1 + " " +  strParam2);
		jcmBatch.start();
		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "***バッチ処理スタートしました");

		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, " END!");
	}

	/**
	 * メール送信要否フラグのチェックを行います。
	 *
	 * @param	String	pcFlg		ＰＣ送信要否フラグ
	 * @param	String	keitaiFlg	携帯送信要否フラグ
	 * @return	int		判定フラグ　１：両方要、２：ＰＣのみ要、３：携帯のみ要、４両方不要
	 */
	protected int chkMailSendFlg(String pcFlg, String keitaiFlg) {

		int ret = 0;

		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "★★★	ＰＣ送信要否フラグ : " + pcFlg);
		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "★★★	携帯送信要否フラグ : " + keitaiFlg);

		// PC送信フラグ：要、携帯送信フラグ：要
		if((pcFlg.equals("1")) && keitaiFlg.equals("1")) {
			ret = 1;
		// PC送信フラグ：要、携帯送信フラグ：不要
		} else if((pcFlg.equals("1")) && keitaiFlg.equals("0")) {
			ret = 2;
		// PC送信フラグ：不要、携帯送信フラグ：要
		} else if((pcFlg.equals("0")) && keitaiFlg.equals("1")) {
			ret = 3;
		// PC送信フラグ：不要、携帯送信フラグ：不要
		} else if((pcFlg.equals("0")) && keitaiFlg.equals("0")) {
			ret = 4;
		}
		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "★★★	判定フラグ : " + ret);
		return ret;
	}

	/**
	 * メール送信要否フラグを元に取得顧客情報の設定を行います
	 *
	 * @param	ArrayList			listInfo	設定顧客情報
	 * @param	JOAL22010101Bean	dataBean	顧客情報
	 * @param	int					chkFlg		判定フラグ　１：両方要、２：ＰＣのみ要、３：携帯のみ要、４両方不要
	 * @param	int					iCount		顧客情報カウンタ
	 * @return	int								トータル顧客情報数
	 */
	protected ArrayList setMemberInfo(ArrayList listInfo, JOAL22010101Bean dataBean, int chkFlg, int iCount) {

		JCMLQToiawaseRegistry toiawaseCommon = new JCMLQToiawaseRegistry();
		JCMCheckUtil checkUtil= new JCMCheckUtil();						// チェック部品の生成
		JOAL22010101Bean wkBean1 = setJOAL22010101Bean(dataBean);			// ワークに退避
		JOAL22010101Bean wkBean2 = setJOAL22010101Bean(dataBean);			// ワークに退避
		String wkKbn = "";													// ＰＣ・携帯区分用ワークエリア
		int iwkCount = iCount;

		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "★★★	会員ＩＤ: " + dataBean.getStrKaiyinID());
		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "★★★	組織コード: " + dataBean.getStrSosikiID());
		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "★★★	カウンタ: " + iwkCount);

		switch (chkFlg) {
			case 1:
				// ドメインチェック
				wkKbn = toiawaseCommon.getPcKeitaiKbn(wkBean1.getStrPcMailAddress());
				// PC携帯区分の設定
				wkBean1.setStrKubun(wkKbn);
				if(!wkKbn.equals(PCKEITAIKBN_SONOTA)) {
					if(checkUtil.isCheckExist(wkBean1.getStrPcMailAddress())) {
						// メールアドレスの設定（PC用）
						wkBean1.setStrAddress(wkBean1.getStrPcMailAddress());
						listInfo.add(iwkCount, wkBean1);
						iwkCount++;
						JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "★★★	ＰＣ用(アドレス): " + wkBean1.getStrAddress());
						JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "★★★	ＰＣ用(アドレス) インデックス: " + iwkCount);
					}
				}

				// ドメインチェック
				wkKbn = toiawaseCommon.getPcKeitaiKbn(wkBean2.getStrKeitaiMailAddress());
				// PC携帯区分の設定
				wkBean2.setStrKubun(wkKbn);
				if(!wkKbn.equals(PCKEITAIKBN_SONOTA)) {
					// メールアドレスの設定（携帯用）
					if(checkUtil.isCheckExist(wkBean2.getStrKeitaiMailAddress())) {
						wkBean2.setStrAddress(wkBean2.getStrKeitaiMailAddress());
						listInfo.add(iwkCount, wkBean2);
						iwkCount++;
						JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "★★★	携帯用（アドレス）: " + wkBean2.getStrAddress());
						JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "★★★	携帯用 (アドレス) インデックス: " + iwkCount);
					}


				}
				// アドレスがＰＣ用、携帯用共に設定されていない時は１レコード作成
				if((!checkUtil.isCheckExist(wkBean1.getStrAddress())) &&
				   (!checkUtil.isCheckExist(wkBean2.getStrAddress()))) {
					listInfo.add(iwkCount, wkBean1);
					iwkCount++;
					JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "★★★	××用（アドレス）: " + wkBean1.getStrAddress());
					JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "★★★	××用 (アドレス) インデックス: " + iwkCount);
				}
				break;

			case 2:
				// ドメインチェック
				wkKbn = toiawaseCommon.getPcKeitaiKbn(wkBean1.getStrPcMailAddress());
				// PC携帯区分の設定
				wkBean1.setStrKubun(wkKbn);
				if(!wkKbn.equals(PCKEITAIKBN_SONOTA)) {
					// メールアドレスの設定（PC用）
					wkBean1.setStrAddress(wkBean1.getStrPcMailAddress());
					JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "★★★	ＰＣ用(アドレス): " + wkBean1.getStrAddress());
				}
				listInfo.add(iwkCount, wkBean1);
				iwkCount++;
				break;

			case 3:
				// ドメインチェック
				wkKbn = toiawaseCommon.getPcKeitaiKbn(wkBean2.getStrKeitaiMailAddress());
				// PC携帯区分の設定
				wkBean2.setStrKubun(wkKbn);
				if(!wkKbn.equals(PCKEITAIKBN_SONOTA)) {
					// メールアドレスの設定（携帯用）
					wkBean2.setStrAddress(wkBean2.getStrKeitaiMailAddress());
					JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "★★★	携帯用（アドレス）: " + wkBean2.getStrAddress());
				}
				listInfo.add(iwkCount, wkBean2);
				iwkCount++;
				break;

			case 4:
				JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "×××	送信フラグが両方 不要！！ 会員ＩＤ: " + dataBean.getStrKaiyinID());
				JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "×××	送信フラグが両方 不要！！ 組織コード: " + dataBean.getStrSosikiID());
				break;
		}

		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "★★★	カウンタ(戻り): " + iwkCount);
		return listInfo;
	}

	/**
	 * データクラスのコピーを行います
	 *
	 * @param	JOAL22010101Bean	dataBean	顧客情報
	 * @return	JOAL22010101Bean				コピー顧客情報数
	 */
	private JOAL22010101Bean setJOAL22010101Bean(JOAL22010101Bean dataBean) {

		JOAL22010101Bean ret = new JOAL22010101Bean();

		ret.setStrAddress(dataBean.getStrAddress());
		ret.setStrAutoBoundID(dataBean.getStrAutoBoundID());
		ret.setStrAutoBoundKeimei(dataBean.getStrAutoBoundKeimei());
		ret.setStrGyoukouCode(dataBean.getStrGyoukouCode());
		ret.setStrGyoukoumei(dataBean.getStrGyoukoumei());
		ret.setStrIsKaiyi(dataBean.getStrIsKaiyi());
		ret.setStrKaien(dataBean.getStrKaien());
		ret.setStrKaiyinID(dataBean.getStrKaiyinID());
		ret.setStrKanaMeimei(dataBean.getStrKanaMeimei());
		ret.setStrKanaSimei(dataBean.getStrKanaSimei());
		ret.setStrKanriNO(dataBean.getStrKanriNO());
		ret.setStrKeitaiMailAddress(dataBean.getStrKeitaiMailAddress());
		ret.setStrKeitaiMailSendFlg(dataBean.getStrKeitaiMailSendFlg());
		ret.setStrKouenCode(dataBean.getStrKouenCode());
		ret.setStrKouenhi(dataBean.getStrKouenhi());
		ret.setStrKouenmei(dataBean.getStrKouenmei());
		ret.setStrLogicDel(dataBean.getStrLogicDel());
		ret.setStrMeimei(dataBean.getStrMeimei());
		ret.setStrNO(dataBean.getStrNO());
		ret.setStrOrderName(dataBean.getStrOrderName());
		ret.setStrOrderStatus(dataBean.getStrOrderStatus());
		ret.setStrOrgSyubetu(dataBean.getStrOrgSyubetu());
		ret.setStrPcMailAddress(dataBean.getStrPcMailAddress());
		ret.setStrPcMailSendFlg(dataBean.getStrPcMailSendFlg());
		ret.setStrRenrakuDenwabango(dataBean.getStrRenrakuDenwabango());
		ret.setStrReplyToAddr(dataBean.getStrReplyToAddr());
		ret.setStrSimei(dataBean.getStrSimei());
		ret.setStrSosikiID(dataBean.getStrSosikiID());
		ret.setStrSousinnaiyou(dataBean.getStrSousinnaiyou());
		ret.setStrSubGyoukouCode(dataBean.getStrSubGyoukouCode());
		ret.setStrTaisyouRenban(dataBean.getStrTaisyouRenban());
		ret.setStrTyuusyutuRenban(dataBean.getStrTyuusyutuRenban());
		ret.setStrUketukeJohoCode(dataBean.getStrUketukeJohoCode());
		ret.setStrUketukeJohoName(dataBean.getStrUketukeJohoName());
		ret.setStrWuketuketyaneru(dataBean.getStrWuketuketyaneru());
		ret.setStrOutBaundKougyoIndex(dataBean.getStrOutBaundKougyoIndex());

		return ret;
	}

	public ArrayList setBeanToArry(ArrayList setData, ArrayList getData) {

		int intCount = getData.size();
		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "☆データサイズ  :[" + intCount + "]");
		for(int i=0;i<intCount;i++) {
			// Bean型へ変換を行います。
			JOAL22010101Bean oJOAL22010101Bean = (JOAL22010101Bean) getData.get(i);
			// インデックスを設定します。
			oJOAL22010101Bean.setStrNO(String.valueOf(setData.size() + 1));
			// リストへセットします。
			setData.add(setData.size(), oJOAL22010101Bean);
		}

		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "☆データ設定終了  :[" + setData + "]");
		return setData;
	}

	public ArrayList setdefModlToArray(ArrayList setData, DefaultListModel getData) {

		int intCount = getData.getSize();
		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "☆データサイズ  :[" + intCount + "]");
		for(int i=0;i<intCount;i++) {
			// リストへセットします。
			setData.add(setData.size(), getData.getElementAt(i));
			// インデックスの再採番は？
		}
		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "☆データ設定終了  :[" + setData + "]");
		return setData;
	}

	/**
	 * アウトバウンド送信内容の削除処理を行います。
	 * @param 	 strAutoBoundID		 アウトバウンドＩＤ
	 * @throws  JOFGyomuException   業務共通部品で致命的エラー(異常)を検知した場合
	 * @throws  SQLException        DBアクセス処理で致命的エラー(異常)を検知した場合
	 */
	public void doSoushinDelete(String strAutoBoundID) throws JOFGyomuException, SQLException {

		JOFDBAccesser dbAccesserSLQ9450 = null;
		ArrayList conSLQ9450 = null;
		ResultSet rsSLQ9450 = null;

		//DBアクセス
		dbAccesserSLQ9450 = new JOFDBAccesser();

		conSLQ9450 = new ArrayList(1);
		conSLQ9450.add(strAutoBoundID);

		//アウトバウンド対象者の削除
		try {
			rsSLQ9450 = dbAccesserSLQ9450.getResultSetBySelect("SLQ9450", conSLQ9450);
		} finally {
			dbAccesserSLQ9450.close();
		}
	}

	/**
	 * アウトバウンド送信内容の削除処理を行います。(区分指定有り)
	 * @param 	 strAutoBoundID		 アウトバウンドＩＤ
	 * @throws  JOFGyomuException   業務共通部品で致命的エラー(異常)を検知した場合
	 * @throws  SQLException        DBアクセス処理で致命的エラー(異常)を検知した場合
	 */
	public void doSoushinDelete_Kubun(String strAutoBoundID, String strKubun) throws JOFGyomuException, SQLException {

		JOFDBAccesser dbAccesserSLQ9460 = null;
		ArrayList conSLQ9460 = null;
		ResultSet rsSLQ9460 = null;

		//DBアクセス
		dbAccesserSLQ9460 = new JOFDBAccesser();

		conSLQ9460 = new ArrayList(2);
		conSLQ9460.add(strAutoBoundID);
		conSLQ9460.add(strKubun);

		//アウトバウンド対象者の削除
		try {
			dbAccesserSLQ9460.execute("SLQ9460", conSLQ9460);
		} finally {
			dbAccesserSLQ9460.close();
		}
	}

	/**
	 * 社内向けにアウトバウンド依頼メールを送信する
	 *
	 * @param	mailSender	メール送信クラス
	 * @param	dataBean	アウトバウンドビーン
	 * @param	numKbn		更新区分(1:新規、2:更新)
	 * @return	boolean							True:正常、false:異常
	 */
	public boolean makeOutBaundInfoMail(JCMInternalSendMail mailSender, JOAL220101Bean dataBean, String numKbn) throws Exception {
// TODO: Inbound
		JCMCheckUtil 		checkUtil 			= new JCMCheckUtil();
		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "アウトバウンド通知メール情報の設定");

		// 利用者名の設定
		String strRiyousya = null;
		try {
			//利用者名称の取得
			if(numKbn.equals("1")) {
				strRiyousya = getRiyousyaName();
			} else {
				// 再送のため登録者コードの取得を行います。
				String strRegCode = getOutBoundRegCode(dataBean.getStrOutBndID().getText());
				strRiyousya = getRiyousyaCodeToName(strRegCode);
			}
		}catch(Exception e){
			return false;
		}

		// 選択している要因情報インデックスを取得する。
		int index = dataBean.getCmbYouiin().getSelectedIndex();

		// 本文の作成
		StringBuffer strHonbun = new StringBuffer();
		if(numKbn.equals("1")) {
			strHonbun.append(strRiyousya).append("さんからメールアウトバウンド依頼がありました。").append(RETURN_CODE);
		} else {
			strHonbun.append(strRiyousya).append("さんからのメールアウトバウンド依頼内容が【更新】されました。").append(RETURN_CODE);
		}
		strHonbun.append("アウトバウンドID：").append(dataBean.getStrOutBndID().getText()).append(RETURN_CODE);
		strHonbun.append("アウトバウンド理由：").append(dataBean.getCmbYouiin().getTextAt(index)).append(RETURN_CODE);

		strHonbun.append("配信希望日：").append(dataBean.getStrHaisinkibou()).append(RETURN_CODE).append(RETURN_CODE);

		// アウトバウンドＩＤより対象の公演情報を取得します。
		ArrayList list = getOutBaundKouenInfo(dataBean.getStrOutBndID().getText());

		StringBuffer wkBuffer = new StringBuffer();
		int iMax = (list == null) ? 0 : list.size();
		for(int i=0; i<iMax; i++) {

			HashMap map = (HashMap)list.get(i);
			wkBuffer.append("---------------------------------------------").append(RETURN_CODE);
			// 受付名称の設定を行います
			wkBuffer.append("受付名：");
			if(map.get("受付名称ＢＡＣＫ用") != null && !map.get("受付名称ＢＡＣＫ用").toString().equals("")) {
				wkBuffer.append(map.get("受付名称ＢＡＣＫ用"));
			}
			wkBuffer.append(RETURN_CODE);

			// 公演名の設定を行います
			wkBuffer.append("公演名：");
			if(map.get("興行名称１") != null && !map.get("興行名称１").toString().equals("")) {
				wkBuffer.append(map.get("興行名称１"));
			}
			wkBuffer.append(RETURN_CODE);

			// 公演日の設定を行います
			wkBuffer.append("公演日：");
			String kouenFlg = map.get("公演日表示フラグ") == null ? "" :  map.get("公演日表示フラグ").toString();
			if(kouenFlg.equals("1")) {
				String kouenMongon = map.get("公演日表示文言") == null ? "" :  map.get("公演日表示文言").toString();
				wkBuffer.append(kouenMongon);
			} else {
				if(map.get("公演日") != null && !map.get("公演日").toString().equals("")) {
					wkBuffer.append(map.get("公演日").toString().substring(0,4)).append("/");
					wkBuffer.append(map.get("公演日").toString().substring(4,6)).append("/");
					wkBuffer.append(map.get("公演日").toString().substring(6,8));
				}
			}
			wkBuffer.append(RETURN_CODE);
			wkBuffer.append("---------------------------------------------").append(RETURN_CODE);
		}
		strHonbun.append(wkBuffer.toString());

		if(checkUtil.isCheckExist(dataBean.getStrMemo().getText())){
			strHonbun.append("メモ：").append(RETURN_CODE);
			strHonbun.append(dataBean.getStrMemo().getText());
		}else{
			strHonbun.append("メモ：なし");
		}

		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "依頼アウトバウンドメール本文" + strHonbun.toString());

		// メール件名を設定
		StringBuffer bufTitle = new StringBuffer();
		if(numKbn.equals("1")) {
			bufTitle.append("【ＯＢ依頼】");
		} else if(numKbn.equals("2")) {
			bufTitle.append("【ＯＢ依頼・更新】");
		}
		setMailTitle(dataBean, bufTitle);
		mailSender.setSubject(bufTitle.toString());

		// メール送信
		// CF用と営業用で2回送る
//JCB対応 start
		// JCBアウトバウンドIDの場合
		if("J".equals(dataBean.getStrOutBndID().getText().substring(0, 1))) {
			// 営業グループの場合、宛先をJCB営業デスクに設定
			if(dataBean.getGroupPtn() == "0") {
				// メール宛先バターン（TO "JCB営業デスク")の設定
				mailSender.addTo("11");
			} else {
				// メール宛先バターン（TO "CF(JCB)")の設定
				mailSender.addTo("10");
			}
		} else {
			// メール宛先バターン（TO "CF")の設定
			mailSender.addTo("7");
		}
//JCB対応 end

		//本文の設定
		mailSender.setHonbun(strHonbun.toString());

		//メール送信
		mailSender.send();
		//設定アドレスのクリア
		mailSender.clearTo();

		// JCBアウトバウンドIDの場合
		if("J".equals(dataBean.getStrOutBndID().getText().substring(0, 1))) {
			// メール宛先バターン（TO "JCB営業デスク")の設定
			mailSender.addTo("12");
		} else {
			//メール宛先バターン（TO "営業")の設定
			mailSender.addTo("8");
		}

		//メール送信
		mailSender.send();
		//設定アドレスのクリア
		mailSender.clearTo();

		return true;
	}

	/**
	 * 公演名、公演日の取得
	 *
	 * @param	String		outBaundId		アウトバウンドＩＤ
	 * @return	ArrayList					公演情報
	 */
	private ArrayList getOutBaundKouenInfo(String outBaundId) {

		ArrayList ret = new ArrayList();
		ArrayList cond = new ArrayList();
		JOFDBAccesser dba = new JOFDBAccesser();

		// アウトバウンドＩＤを条件に設定
		cond.add(outBaundId);

		try {
			JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "SLQ4474(条件) ： " + cond);
			ret = dba.select("SLQ4474", cond);
			JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "SLQ4474(結果) ： " + ret);
		} catch (Exception e) {
			return null;
		}
		return ret;
	}

	/**
	 * アウトバウンド通知メール件名設定
	 *
	 * @param	JOAL220101Bean		dataBean	アウトバウンドビーン
	 * @param	StringBuffer		bufTitle	メールタイトル情報
	 */
	private void setMailTitle(JOAL220101Bean dataBean, StringBuffer bufTitle) throws JOFGyomuException, SQLException {

		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "setMailTitle(設定前) ： " + bufTitle.toString());
		String wkKougyouName = "";

		// 画面より興行コードを取得する。strKougyoCode
		String kougyou = dataBean.getStrKougyoCode().getText();

		//興行サブコード情報を取得する
		String[][] kougyoSubInfo = null;
		kougyoSubInfo = getKougyoSubInfo(kougyou);

		if(kougyoSubInfo != null){
			wkKougyouName = kougyoSubInfo[0][2];
		}
		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "興行名称 ： " + wkKougyouName);

		// 選択している要因情報を取得する。
		int index = dataBean.getCmbYouiin().getSelectedIndex();
		String strYouin = dataBean.getCmbYouiin().getTextAt(index);

		// 件名情報を設定する
		bufTitle.append(wkKougyouName).append("/").append(strYouin);
		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "setMailTitle(設定後) ： " + bufTitle.toString());

		return ;
	}

	/**
	 * アウトバウンド興行抽出情報のインデックスを取得します。
	 *
	 * @param	String			kougyou		興行コード
	 * @param	String			kougyouSub	興行サブコード
	 * @param	String			uketsuke	受付情報コード
	 * @param	String			kougyouMei	興行名
	 * @param	String			kouen		公演コード
	 * @param	String			kouenbi		公演日
	 * @param	String			kaien		開演
	 * @param	String			status		オーダーステータス
	 * @return	int							インデックス
	 */
	private String setOutBoundKougyouIndex(HashMap map,
										  String kougyou, String kougyouSub, String uketsuke, String kougyouMei,
										  String kouen, String kouenbi, String kaien, String status) {

		StringBuffer wkBuf = new StringBuffer();
		String strIndex = "0";

		// ＫＥＹ情報の作成
		wkBuf.append(kougyou).append(":")
			 .append(kougyouSub).append(":")
			 .append(uketsuke).append(":")
			 .append(kougyouMei).append(":")
			 .append(kouen).append(":")
			 .append(kouenbi).append(":")
			 .append(kaien).append(":")
			 .append(status);
		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "抽出時のＫＥＹ情報 ： " + wkBuf.toString());

		if(map.get(wkBuf.toString()) == null)  {
			strIndex = String.valueOf(map.size() + 1);
			map.put(wkBuf.toString(), strIndex);
		} else {
			strIndex = map.get(wkBuf.toString()).toString();
		}
		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "抽出時のＫＥＹ情報(マップインデックス) ： " + strIndex);
		return strIndex;
	}

	/**
	 * アウトバウンド興行抽出情報のインデックスを取得します。
	 *
	 * @param	String			kougyou		興行コード
	 * @param	String			kougyouSub	興行サブコード
	 * @param	String			uketsuke	受付情報コード
	 * @param	String			kougyouMei	興行名
	 * @param	String			kouen		公演コード
	 * @param	String			kouenbi		公演日
	 * @param	String			kaien		開演
	 * @param	String			status		オーダーステータス
	 * @return	int							インデックス
	 */
	private String getOutBoundKougyouIndex(HashMap map,
										  String kougyou, String kougyouSub, String uketsuke, String kougyouMei,
										  String kouen, String kouenbi, String kaien, String status) {

		StringBuffer wkBuf = new StringBuffer();
		String strIndex = "0";

		// ＫＥＹ情報の作成
		wkBuf.append(kougyou).append(":")
			 .append(kougyouSub).append(":")
			 .append(uketsuke).append(":")
			 .append(kougyouMei).append(":")
			 .append(kouen).append(":")
			 .append(kouenbi).append(":")
			 .append(kaien).append(":")
			 .append(status);
		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "抽出時のＫＥＹ情報 ： " + wkBuf.toString());

		if(map.get(wkBuf.toString()) == null)  {
		} else {
			strIndex = map.get(wkBuf.toString()).toString();
		}
		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "抽出時のＫＥＹ情報(マップインデックス) ： " + strIndex);
		return strIndex;
	}

	/**
	 * アウトバウンドステータスの更新を行います。<br>
	 *
	 * @param strOutBaundId アウトバウンドＩＤ
	 * @param status 更新後のステータス
     */
	public void setOutBaoundStatus(String strOutBaundId, String status) {

		ArrayList		cond		= new ArrayList();
		JOFDBAccesser	dbAccesser	= new JOFDBAccesser();
		int result = 0;

		// 条件を設定する
		cond.add(status);
		cond.add(JOFUtil.getRegUserID());
		cond.add(CLASS_NAME);
		cond.add(JOFUtil.getConsoleID());
		cond.add(strOutBaundId);

		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "更新者    ： " + JOFUtil.getRegUserID());
		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "更新プロ  ： " + CLASS_NAME);
		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "更新端末  ： " + JOFUtil.getConsoleID());
		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "アウトＩＤ： " + strOutBaundId);

		// ＬＴアウトバウンド管理のステータスを更新します。
		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "SLQ4454 (条件) ： " + cond);
		try {
			result = dbAccesser.execute("SLQ4454", cond);
		} catch (Exception e) {
			JCMLog.errorLog(JCMLQConstants.ERROR_LOG_LEVEL, "ML1008E", "アウトバウンドステータス更新エラー", e);
		} finally {
			dbAccesser.close();
		}
		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "SLQ4454 (結果) ： " + result);
		return ;
	}

	/**
	 * アウトバンドＩＤのオーダー件数を取得する
	 * @param strOutBndID 	アウトバンドＩＤ
	 */
	public String getTotalCnt(String strOutBndID) throws Exception {

		ArrayList retLstNaiyou = new ArrayList();
		JOFDBAccesser dbAccesser = new JOFDBAccesser();
		ArrayList conSQL = new ArrayList(1);
		ResultSet rsSQL = null;

		conSQL.add(strOutBndID);

		try {
			rsSQL = dbAccesser.getResultSetBySelect("SLQ4487", conSQL);
			rsSQL.next();
			JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "SLQ4487 (結果) ： " + rsSQL.getString(1));
			return rsSQL.getString(1);
		} catch (Exception e) {
			return "";
		} finally {
			dbAccesser.close();
		}
	}

	/**
	 * ＬＴアウトバウンド管理をロックする
	 * @param strKubun		区分
	 * @param strOutBndID 	アウトバウンドＩＤ
	 * @return boolean		true：ロック取得 false:ロック未取得
	 */
	public boolean getLockOutBoundId(String strKubun,String strOutBndID) throws Exception {

		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "START");

		ArrayList retLst = new ArrayList();
		ArrayList conSQL = new ArrayList(1);
		JOFDBAccesser dbAccesser = new JOFDBAccesser();

		String sqlId = "";//ＳＱＬＩＤ
		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "区分：" +strKubun);

		conSQL.add(strOutBndID);

		//ＬＴアウトバンド管理の存在チェック
		//区分  1:抽出ボタン押下時のロック
		//      2:送信ボタン押下時のロック
		if("1".equals(strKubun)){
			sqlId = "SLQ4471";
			//ＳＱＬ実行
			retLst = dbAccesser.select(sqlId, conSQL);
			if(retLst == null  || retLst.size()==0){
				//未依頼、未送信のためＯＫ
				JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "未依頼・未送信状態のためＯＫ アウトバウンドＩＤ：" + strOutBndID);
				return true;
			}
		}

		//区分  1:抽出ボタン押下時のロック
		//      2:送信ボタン押下時のロック
		sqlId="SLQ4491";
		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "ＳＱＬＩＤ：" +sqlId);
		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "アウトバウンドＩＤ：" + strOutBndID);

		try {
			//select for updateを実行する
			retLst = dbAccesser.select(sqlId, conSQL);
			JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, sqlId +"(結果) ： " + retLst);
			if(retLst != null  && retLst.size()!=0){
				//ロック取得
				JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "ＬＴアウトバウンド管理　ロックＯＫ");
				//取得ＯＫ、区分が1(抽出ボタン押下時ロック)の場合、配信ステータスＩＤを確認
				if("1".equals(strKubun)){
					String strHaishinId = (((HashMap)retLst.get(0)).get("配信ステータスＩＤ")).toString();
					if("002".equals(strHaishinId)){
						//配信ステータスＩＤ：002（送信中）のためＮＧ
						JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "ＬＴアウトバウンド管理　配信ステータス:002 処理続行ＮＧ");
						return false ;
					}
				}
				return true ;

			}else{
				//ロック未取得
				JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "ＬＴアウトバウンド管理　ロックＮＧ");
				return false ;
			}
		} catch (JOFFrameworkRuntimeException e) {
			//ロック未取得
			JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "ＬＴアウトバウンド管理　ロックＮＧ");
			return false ;
		} catch (JOFGyomuException ex) {
			//ロック未取得
			JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "ＬＴアウトバウンド管理　ロックＮＧ");
			return false ;
		} finally {
			dbAccesser.close();
		}
	}

	/**
	 * 「ＬＴアウトバウンド対象者」,「ＬＴアウトバウンド対象者詳細」に
	 * 新会員の情報を反映させます。
	 * @param outBnd_id アウトバウンドID
	 * @throws JOFGyomuException
	 */
	public void setShinKaiinJoho(String outBnd_id) throws JOFGyomuException{

		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "ＬＴアウトバウンド対象者　新会員情報の反映　開始");

		//更新が必要な会員ＩＤを取得
		ArrayList searchKaiinMapList = getSearchKaiinJoho(outBnd_id);

		if(searchKaiinMapList.isEmpty()){
			JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "ＬＴアウトバウンド対象者　対象レコード無し　新会員情報の反映 終了");
			return;
		}

		//ＡＰＩ実行のため、会員情報map→beanに詰め替え
		JCMWShinKaiinApiIoBean[] searchKaiinBeanList = convertMapToBean(searchKaiinMapList);

		JCMWSinkaiinAccesser accesser = JCMWSinkaiinAccesser.getAccesser(); // アクセスインスタンス

		//新会員の情報を取得（複数会員検索ＡＰＩ）
		JCMWShinKaiinApiArrayResultBean arrayResultBean = accesser.getSearchMemberById(searchKaiinBeanList
																		, JCMWMemberSearchApi.DETAILS_SHUTOKU_DIV_DETAILED
																		, JCMWMemberSearchApi.SEATCH_DIV_RYOHOU);

		//検索失敗ならばエラー
		if(!"200".equals(arrayResultBean.getStatusCode())){
			JCMLog.debugLog(JOALConstant.DEBUG_LOG_LEVEL1,"複数会員検索APIエラー_ステータスコード:" + arrayResultBean.getStatusCode());
			throw new JOFGyomuRuntimeException(new Exception(), "ML0410E", JCMMessageManager.getMessage("ML0410E"));
		}

		//新会員取得結果より、アウトバウンド更新用のBeanを生成
		List outBndKaiinList = makeOutBndList(arrayResultBean.getResultList());//new ArrayList();

		//アウトバウンドを更新する。
		updateOutBnd(outBnd_id, outBndKaiinList);
	}

	/**
	 * ＬＴアウトバウンド対象者から、新会員IDの一覧を取得する。
	 * @param outBnd_id
	 * @return
	 */
	private ArrayList getSearchKaiinJoho(String outBnd_id){

		ArrayList conditions = new ArrayList(1);			// 検索条件
		conditions.add(outBnd_id);
		ArrayList 	result 		= null;			// 検索結果(From SQL)

		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "新会員情報が必要なアウトバウンドの抽出　開始");

		// インスタンスの生成と取得
		JOFDBAccesser dbAccesser = new JOFDBAccesser();		// DBアクセスインスタンス
		// DBアクセス
		result = new JCMLMWebCommonDB().execSelectSql(dbAccesser, "SLM9226", conditions);

		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "新会員情報が必要なアウトバウンドの抽出　終了");

		return result;
	}


	/**
	 * HashMapの配列から、Beanの配列に変換する。
	 * @param searchKaiinMapList
	 * @return
	 */
	private JCMWShinKaiinApiIoBean[] convertMapToBean(ArrayList searchKaiinMapList){
		JCMWShinKaiinApiIoBean[] searchKaiinBeanList = new JCMWShinKaiinApiIoBean[searchKaiinMapList.size()];

		for(int i =0; i < searchKaiinMapList.size(); i++){

			JCMWShinKaiinApiIoBean shinkainJoho = new JCMWShinKaiinApiIoBean();

			HashMap kaiinMap = (HashMap) searchKaiinMapList.get(i);

			shinkainJoho.setSoshikiCode((String)kaiinMap.get("組織コード"));
			shinkainJoho.setMemberId( (String)kaiinMap.get("会員ＩＤ"));

			searchKaiinBeanList[i] = shinkainJoho ;
		}
		return searchKaiinBeanList;
	}


	/**
	 * アウトバウンド対象者へ登録するBeanのリストを生成する。
	 * @param sinMemberList
	 * @return
	 */
	private List makeOutBndList(List sinMemberList){

		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "ＬＴアウトバウンド対象者　更新用Bean生成　開始");
		List outBndKaiinList = new ArrayList();

		for(int i =0; i < sinMemberList.size(); i++){
			//新会員から取得したBean：fromBean
			JCMWCustomerBean fromBean = (JCMWCustomerBean) sinMemberList.get(i);
			//アウトバウンドに登録するBean:toBean
			JOAL220102Bean toBean = new JOAL220102Bean();

			//組織コード、会員ID
			toBean.setSoshikiCd(fromBean.getSoshikiCode());
			toBean.setKaiinID(fromBean.getMemberId());

			//氏名
			toBean.setKanjiSei(fromBean.getLastName());
			toBean.setKanjiMei(fromBean.getFirstName());
			toBean.setKanaSei(fromBean.getLastKanaName());
			toBean.setKanaMei(fromBean.getFirstKanaName());

			//電話番号：携帯電話を優先にする。
			String denwaNo =  fromBean.getMobileTelnum();
			if(denwaNo == null || "".equals(denwaNo)){
				denwaNo =  fromBean.getTelnum();
			}
			toBean.setDenwaNo(denwaNo);

			//メールアドレス
			toBean.setPcMailAddress(fromBean.getMailAddress1());
			toBean.setKeitaiMailAddress(fromBean.getMailAddress2());

			//生成した結果をリストにセット
			outBndKaiinList.add(toBean);
		}

		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "ＬＴアウトバウンド対象者　更新用Bean生成　終了");
		return outBndKaiinList;
	}

	/**
	 * アウトバウンド対象者、アウトバウンド対象者詳細を更新する。
	 * @param outBndKaiinList
	 */
	private void updateOutBnd(String outBnd_id, List outBndKaiinList){

		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "ＬＴアウトバウンド対象者　update処理　開始");

		JOFDBAccesser dbAccesser =  new JOFDBAccesser();
		JCMLMWebCommonDB commonDB = new JCMLMWebCommonDB();

		String kanriNo = "";
		String soshikiCd = "";
		String kaiinId = "";

		try{
			ArrayList conditions = new ArrayList();
			conditions.add(outBnd_id);		//アウトバウンドＩＤ
			//アウトバウンド情報取得
			ArrayList resultList = commonDB.execSelectSql(dbAccesser, "SLQ4486", conditions);

			ArrayList outBountList = new ArrayList();
			for(int i =0; i < resultList.size(); i++){
				HashMap kaiinMap = (HashMap) resultList.get(i);
				if (kaiinMap.get("会員ＩＤ") == null) {
					continue;
				}
				String[] outBountInfo = new String[3];
				outBountInfo[0] = kaiinMap.get("管理番号").toString();
				outBountInfo[1] = kaiinMap.get("組織コード").toString();
				outBountInfo[2] = kaiinMap.get("会員ＩＤ").toString();

				outBountList.add(outBountInfo);
			}

			for(int i =0; i < outBountList.size(); i++){
				String[] obInfo = (String[]) outBountList.get(i);
				kanriNo = obInfo[0];
				soshikiCd = obInfo[1];
				kaiinId = obInfo[2];
				for(int j =0; j < outBndKaiinList.size(); j++){

					JOAL220102Bean kaiinBean = (JOAL220102Bean) outBndKaiinList.get(j);

					if(!(obInfo[1].equals(kaiinBean.getSoshikiCd())
							&& obInfo[2].equals(kaiinBean.getKaiinID()))) {
						continue;
					}

					//ＬＴアウトバウンド対象者詳細を更新
					conditions = new ArrayList(14);

					//パラメーターセット
					conditions.add(kaiinBean.getKanjiSei());		//漢字氏名＿姓
					conditions.add(kaiinBean.getKanjiMei());		//漢字氏名＿名
					conditions.add(kaiinBean.getKanaSei());			//カナ氏名＿姓
					conditions.add(kaiinBean.getKanaMei());			//カナ氏名＿名
					conditions.add(kaiinBean.getPcMailAddress());		//PCメールアドレス
					conditions.add(kaiinBean.getKeitaiMailAddress());	//携帯メールアドレス
					conditions.add(kaiinBean.getDenwaNo());				//連絡先電話番号

					conditions.add(JCMLMCommonDB.getBussinessDate());	// 更新日
					conditions.add(JOFUtil.getRegUserID());				// 更新者
					conditions.add(PROGRAM_ID);							// 更新プログラムＩＤ
					conditions.add(JOFUtil.getConsoleID());				// 更新端末ＩＤ

					conditions.add(outBnd_id);						//アウトバウンドＩＤ
					conditions.add(kaiinBean.getSoshikiCd());		//組織コード
					conditions.add(kaiinBean.getKaiinID());			//会員ＩＤ

					commonDB.execExecuteSql(dbAccesser, "SLM9228", conditions);


					//ＬＴアウトバウンド対象者を更新
					//メールアドレス１の更新
					conditions = new ArrayList(15);

					//パラメーターセット
					conditions.add(kaiinBean.getKanjiSei());		//漢字氏名＿姓
					conditions.add(kaiinBean.getKanjiMei());		//漢字氏名＿名
					conditions.add(kaiinBean.getKanaSei());			//カナ氏名＿姓
					conditions.add(kaiinBean.getKanaMei());			//カナ氏名＿名
					conditions.add(kaiinBean.getPcMailAddress());	//メールアドレス
					conditions.add(kaiinBean.getDenwaNo());			//連絡先電話番号
					conditions.add(PCKEITAIKBN_PC);					//ＰＣ＿携帯＿ＴＥＬ区分

					conditions.add(JCMLMCommonDB.getBussinessDate());	// 更新日
					conditions.add(JOFUtil.getRegUserID());				// 更新者
					conditions.add(PROGRAM_ID);							// 更新プログラムＩＤ
					conditions.add(JOFUtil.getConsoleID());				// 更新端末ＩＤ

					conditions.add(outBnd_id);							//アウトバウンドＩＤ
					conditions.add(kaiinBean.getSoshikiCd());		//組織コード
					conditions.add(kaiinBean.getKaiinID());			//会員ＩＤ
					conditions.add(kanriNo);						//管理番号

					//ＳＱＬ発行
					commonDB.execExecuteSql(dbAccesser, "SLM9227", conditions);
					JCMCheckUtil checkUtil = new JCMCheckUtil();
					if (checkUtil.isCheckExist(kaiinBean.getKeitaiMailAddress())) {
						//ＬＴアウトバウンド対象者を登録

						conditions = new ArrayList();
						conditions.add(outBnd_id);		//アウトバウンドＩＤ
						//アウトバウンド連番の最大値+1を取得
						ArrayList ret = commonDB.execSelectSql(dbAccesser, "SLQ4493", conditions);
						String strNextOBSeq = ((HashMap)(ret.get(0))).get("アウトバウンド連番").toString();

						//メールアドレス２の登録
						conditions = new ArrayList(20);

						//パラメーターセット
						conditions.add(outBnd_id);			//アウトバウンドＩＤ
						conditions.add(strNextOBSeq);		//アウトバウンド連番
						conditions.add(kaiinBean.getSoshikiCd());			//組織コード
						conditions.add(kaiinBean.getKaiinID());			//会員ＩＤ
						conditions.add(kaiinBean.getKanjiSei());				//漢字氏名＿姓
						conditions.add(kaiinBean.getKanjiMei());				//漢字氏名＿名
						conditions.add(kaiinBean.getKanaSei());				//カナ氏名＿姓
						conditions.add(kaiinBean.getKanaMei());				//カナ氏名＿名
						conditions.add(kaiinBean.getKeitaiMailAddress());	//メールアドレス
						conditions.add(kaiinBean.getDenwaNo());			//連絡先電話番号
						conditions.add(PCKEITAIKBN_PC);					//ＰＣ＿携帯＿ＴＥＬ区分
						conditions.add(null);										//固有番号
						conditions.add(null);										//アウトバウンド連番＿興行
						conditions.add(kanriNo);									//管理番号

						conditions.add(JOFUtil.getRegUserID());				//登録者
						conditions.add(PROGRAM_ID);							//登録プログラムＩＤ
						conditions.add(JOFUtil.getConsoleID());				//登録端末ＩＤ
						conditions.add(JOFUtil.getRegUserID());				// 更新者
						conditions.add(PROGRAM_ID);							// 更新プログラムＩＤ
						conditions.add(JOFUtil.getConsoleID());				// 更新端末ＩＤ

						//ＳＱＬ発行
						commonDB.execExecuteSql(dbAccesser, "SLQ9390", conditions);
					}
				}
			}
		}catch(Exception e){
				JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "ＬＴアウトバウンド対象者,対象者詳細の更新に失敗"
							+ " アウトバウンドＩＤ:" + outBnd_id
							+ " 組織コード:" + soshikiCd
							+ " 会員ＩＤ:" + kaiinId
							+ " 管理番号:" + kanriNo);
				throw new JOFGyomuRuntimeException(e);
		}
			JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "ＬＴアウトバウンド対象者　update処理　終了");
	}

	/**
	 * 初回抽出時(アウトバウンドID指定なし)用
	 * アウトバウンドIDを元にEMTGトレード対象の受付が存在するかチェックします。
	 *
	 * @param dataBean
	 * @return true:トレード対象あり false:トレード対象なし
	 * @throws Exception
	 */
	protected boolean isTradeUketsukeCheckNotSpecify(String outBoundId) throws JOFGyomuException {

		ArrayList conditions = new ArrayList();

		conditions.add(outBoundId);									// アウトバウンドID
		conditions.add(JOABConstant.SEIRITSU_HENKIN_HOSHIKI_EMTG);	// 成立時返金方式「2：EMTG」

		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "isTradeUketsukeCheckNotSpecify　開始");

		JOFDBAccesser dbAccesser = new JOFDBAccesser();
		ArrayList result = new JCMLMWebCommonDB().execSelectSql(dbAccesser, "SLQ4550", conditions);
		if(null == result || 1 > result.size()) {
			JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "isTradeUketsukeCheckNotSpecify　終了 トレード対象件数取得失敗");
			return false;
		}
		int tradeKensu = Integer.parseInt(((HashMap)(result.get(0))).get("カウント").toString());
		if (1 > tradeKensu) {
			JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "isTradeUketsukeCheckNotSpecify　終了 トレード対象なし");
			return false;
		}
		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "isTradeUketsukeCheckNotSpecify　終了トレード対象あり");
		return true;
	}

	/**
 	 * アウトバウンドID指定あり
	 * アウトバウンドIDを元にEMTGトレード対象の受付が存在するかチェックします。
	 * @param dataBean
	 * @return true:トレード対象あり false:トレード対象なし
	 * @throws Exception
	 */
	protected boolean isTradeUketsukeCheckSpecify(JOAL220101Bean dataBean) throws JOFGyomuException {

		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "isTradeUketsukeCheckSpecify　開始");
		ArrayList conditions = new ArrayList();
		String addSql = "";
		String outBoundId = "";
		// アウトバウンドIDを特定する。
		if(!"".equals(dataBean.getStrOutBndID().getText()) && null != dataBean.getStrOutBndID().getText()){
			outBoundId = dataBean.getStrOutBndID().getText();
		} else if (!"".equals(dataBean.getStrJcbOutBndID().getText()) && null != dataBean.getStrJcbOutBndID().getText()) {
			outBoundId = dataBean.getStrJcbOutBndID().getText();
		} else {
			JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "isTradeUketsukeCheckSpecify　アウトバウンドIDなし");
			return false;
		}

		conditions.add(outBoundId);	// アウトバウンドID
		// EMTGトレード時デフォルト送信メールアドレス(EMTG、トレードマン)
		String emtgDefaultMailAddress = JCMCommonProperties.getProperty("L_EMTG_DEFAULT_MAIL_ADDRESS");
		String[] emtgDefaultMailAddressList = emtgDefaultMailAddress.split(",");
		// 取得したメールアドレスを条件に加える
		if (0 < emtgDefaultMailAddressList.length) {
			addSql = addSql + "AND \"メールアドレス\" IN (";
			for (int i = 0; i < emtgDefaultMailAddressList.length; i++) {
				addSql = addSql + "?,";
				conditions.add(emtgDefaultMailAddressList[i]);
			}
			addSql = addSql.substring(0,addSql.length()-1);
			addSql = addSql + ")";
		} else {
			// メールアドレスが取得できなかった場合
			JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "isTradeUketsukeCheckSpecify　デフォルトメールアドレス取得失敗");
			throw new JOFGyomuRuntimeException();
		}

		// 実行
		JOFDBAccesser dbAccesser = new JOFDBAccesser();
		ArrayList result = new JCMLMWebCommonDB().execSelectSql(dbAccesser, "SLQ4551", conditions, addSql);
		if(null == result || 1 > result.size()) {
			JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "isTradeUketsukeCheckSpecify　終了 トレード対象件数取得失敗");
			return false;
		}
		int tradeKensu = Integer.parseInt(((HashMap)(result.get(0))).get("カウント").toString());
		if (1 > tradeKensu) {
			JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "isTradeUketsukeCheckSpecify　終了 トレード対象なし");
			return false;
		}
		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "isTradeUketsukeCheckSpecify　終了トレード対象あり");
		return true;
	}

	/**
	 * LTアウトバウンド対象者、LTアウトバウンド対象者詳細にデフォルトメールアドレスを設定します。
	 *
	 * @param outBoundId アウトバウンドID
	 * @throws SQLException　JOFGyomuException
	 */
	protected void insertEmtgDefaultMailAddress(String outBoundId) throws Exception {

		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "insertEmtgDefaultMailAddress　開始");

		ArrayList conditions = new ArrayList();
		JOFDBAccesser dbAccesser = new JOFDBAccesser();
		JCMLMWebCommonDB commonDb = new JCMLMWebCommonDB();

		//アウトバウンド連番の最大値+1を取得
		conditions.add(outBoundId); // アウトバウンドID
		ArrayList taishoShaRet = commonDb.execSelectSql(dbAccesser, "SLQ4493", conditions);
		int outBoundRenBan = JCMBUtilityCommon.objToInt(((HashMap)(taishoShaRet.get(0))).get("アウトバウンド連番"));
		if (0 == outBoundRenBan){ // outBoundRenBanはnull置き換え済み
			outBoundRenBan = 1; // レコード無しの場合は1を設定する
		}
		// EMTGトレード時デフォルト送信メールアドレス取得(EMTG、トレードマン)
		String emtgDefaultMailAddress = JCMCommonProperties.getProperty("L_EMTG_DEFAULT_MAIL_ADDRESS");
		String[] emtgDefaultMailAddressList = emtgDefaultMailAddress.split(",");
		if (1 > emtgDefaultMailAddressList.length) {
			// メールアドレスが取得できなかった場合
			JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "insertEmtgDefaultMailAddress　デフォルトメールアドレス取得失敗");
			throw new JOFGyomuException();
		}
		for (int i = 0; i < emtgDefaultMailAddressList.length; i++) {
			// LTアウトバウンド対象者
			conditions.clear();
			conditions.add(outBoundId); 						// アウトバウンドID
			conditions.add(String.valueOf(outBoundRenBan+i));	// アウトバウンド連番
			conditions.add("0000"); 							// 組織コード
			conditions.add("999999999"); 						// 会員ID
			conditions.add("Tixplus");							// 漢字氏名＿姓
			conditions.add("太郎");								// 漢字氏名＿名
			conditions.add("ティックスプラス");						// カナ氏名＿姓
			conditions.add("タロウ");								// カナ氏名＿名
			conditions.add(emtgDefaultMailAddressList[i]);		// メールアドレス
			conditions.add("9999999999");						// 連絡先電話番号
			conditions.add("0");								// ＰＣ＿携帯＿ＴＥＬ区分
			conditions.add("");									// 固有番号
			conditions.add("");									// アウトバウンド連番＿興行
			conditions.add("");									// 管理番号
			conditions = setKanriItem(conditions); 				// システム項目値
			commonDb.execExecuteSql(dbAccesser, "SLQ9390", conditions);
		}
		conditions.clear();
		conditions.add(outBoundId); // アウトバウンドID
		//詳細レコード通番の最大値を取得
		ArrayList taishoShaShosaiRet = commonDb.execSelectSql(dbAccesser, "SLQ4472", conditions);
		int shosaiRecodeTsuBan = JCMBUtilityCommon.objToInt(((HashMap)(taishoShaShosaiRet.get(0))).get("MAX(\"詳細レコード通番\")"));
		if (0 == shosaiRecodeTsuBan){ // shosaiRecodeTsuBanはnull置き換え済み
			shosaiRecodeTsuBan = 1; // レコード無しの場合は1を設定する
		} else {
			// 詳細レコード通番の次番を設定
			shosaiRecodeTsuBan = shosaiRecodeTsuBan + 1;
		}
		for (int i = 0; i < emtgDefaultMailAddressList.length; i++) {
			// LTアウトバウンド対象者詳細
			conditions.clear();
			conditions.add(outBoundId); 							// アウトバウンドID
			conditions.add(String.valueOf(shosaiRecodeTsuBan+i)); 	// 詳細レコード通番
			conditions.add("0000"); 								// 組織コード
			conditions.add("999999999"); 							// 会員ID
			conditions.add("Tixplus");								// 漢字氏名＿姓
			conditions.add("太郎");									// 漢字氏名＿名
			conditions.add("ティックスプラス");							// カナ氏名＿姓
			conditions.add("タロウ");									// カナ氏名＿名
			conditions.add(emtgDefaultMailAddressList[i]);			// PCメールアドレス
			conditions.add("9999999999");							// 電話番号
			conditions.add("999");								// 発送先住所（郵便番号１）
			conditions.add("9999");								// 発送先住所（郵便番号２）
			conditions = setKanriItem(conditions); 					// システム項目値
			commonDb.execExecuteSql(dbAccesser, "SLQ4552", conditions);
		}
		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "insertEmtgDefaultMailAddress　終了");
	}
	/**
	 * 取込ファイルのトレード対象チェック
	 * アウトバウンドIDを元にEMTGトレード対象の受付が存在するかチェックします。
	 * @param dataBean
	 * @return true:トレード対象あり false:トレード対象なし
	 * @throws JOFGyomuException
	 */
	protected boolean isTradeUketsukeCheckFromImport(JOAL220101Bean dataBean) throws JOFGyomuException {
		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "isTradeUketsukeCheckFromImport　開始");
		JCMCheckUtil checkUtil = new JCMCheckUtil();
		// 「抽出結果に追加」にチェックがある場合、
		// 追加元となるアウトバウンドIDにEMTGデフォルトメールが存在するかチェックする
		if (dataBean.getChkKekkaTuika().isChecked()
				&& (checkUtil.isCheckExist(dataBean.getStrOutBndID().getText()))){
			if(isTradeUketsukeCheckSpecify(dataBean)) {
				// EMTGデフォルトメールが登録されていた場合

				// EMTGトレード対象オーダーが存在する旨のダイアログを表示する
				dataBean.setTradeDialogHyojiFlag(true);
				dataBean.setTradeDialogMessage(JCMMessageManager.getMessage("ML1001I"));
				// 送信確認用メッセージ
				dataBean.setTradeSoushinDialogHyojiFlag(true);
				dataBean.setTradeSoushinDialogMessage(JCMMessageManager.getMessage("ML1002I"));
				return false;
			}
		}
		// 「抽出結果に追加」にチェックがある場合かつ、追加元となるアウトバウンドIDにEMTGデフォルトメールが存在しない場合
		// または、「抽出結果に追加」にチェックがない場合はLTアウトバウンドIDの受付、または管理番号を元に
		// トレード対象かチェックする

		// 受付情報を元にトレード対象かチェック
		if (isTradeUketsukeCheckNotSpecify(dataBean.getStrOutBndID().getText())) {
			return true;
		} else {
			// ＬＴアウトバウンド対象者詳細の管理番号を元にトレード対象かチェック
			ArrayList conditions = new ArrayList();
			conditions.add(dataBean.getStrOutBndID().getText());		// アウトバウンドID
			conditions.add(JOABConstant.SEIRITSU_HENKIN_HOSHIKI_EMTG);	// 成立時返金方式「2：EMTG」

			JOFDBAccesser dbAccesser = new JOFDBAccesser();
			ArrayList result = new JCMLMWebCommonDB().execSelectSql(dbAccesser, "SLQ4554", conditions);
			if(null == result || 1 > result.size()) {
				JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "isTradeUketsukeCheckFromImport　終了 トレード対象件数取得失敗");
				return false;
			}
			int tradeKensu = Integer.parseInt(((HashMap)(result.get(0))).get("カウント").toString());
			if (1 > tradeKensu) {
				JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "isTradeUketsukeCheckFromImport　終了 トレード対象なし");
				return false;
			}
			JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "isTradeUketsukeCheckFromImport　終了トレード対象あり");
			return true;
		}
	}
	/**
	 * 管理項目を設定します。
	 * @param conditions
	 * @return
	 */
	private ArrayList setKanriItem(ArrayList conditions) {
		// システム管理項目
		conditions.add(JOFUtil.getRegUserID());
		conditions.add(CLASS_NAME);
		conditions.add(JOFUtil.getConsoleID());
		conditions.add(JOFUtil.getRegUserID());
		conditions.add(CLASS_NAME);
		conditions.add(JOFUtil.getConsoleID());
		return conditions;
	}

	/**
	 * インバウンド購入者が居るか
	 * @param id アウトバウンドID
	 * @return
	 * @throws Exception
	 */
	public boolean hasInboundOrder(String id) throws Exception {
		JOFDBAccesser dba = new JOFDBAccesser();
		ArrayList condition = new ArrayList(1);
		condition.add(id);

		try {
			ArrayList result = dba.select("SLQ4555", condition);
			Map map = (HashMap) result.get(0);

			return JCMBUtilityCommon.objToInt(map.get("オーダー数")) > 0;
		} finally {
			dba.close();
		}
	}
	/**
	 * 多言語アウトバウンド内容を取得する
	 * @param id アウトバウンドID
	 * @return
	 * @throws Exception
	 */
	public HashMap<Language, JOALMailTemplate> getInboundOutboundInfo(String id, String jcbId) throws Exception {
		HashMap<Language, JOALMailTemplate> result = new HashMap<>();
		JOFDBAccesser dba = new JOFDBAccesser();
		ArrayList condition = new ArrayList(1);
		condition.add(id);
		condition.add(jcbId);

		try {
			ResultSet rs = dba.getResultSetBySelect("SLQ4494", condition);
			while (rs.next()) {
				Language lang = Language.get(rs.getString(1));
				if (lang == null) {
					throw new JOFGyomuRuntimeException(new Exception(), "ML0410E", JCMMessageManager.getMessage("ML0410E"));
				}

				JOALMailTemplate mt = new JOALMailTemplate();
				mt.setKenmei(rs.getString(2));
				mt.setNaiyou(rs.getString(3));
				result.put(lang, mt);
			}
			return result;
		} catch (Exception e) {
			throw e;
		}finally {
			dba.close();
		}
	}
	/**
	 * アウトバウンド内容をBeanの各言語毎に設定する
	 * @param dataBean
	 * @param map 言語毎のアウトバウンド送信内容
	 */
	public void setInboundOutboundInfo(JOAL220101Bean dataBean, Map<Language, JOALMailTemplate> map) {
		for (Entry<Language, JOALMailTemplate> item: map.entrySet()) {
			FieldString kenmei = new FieldString(item.getValue().getKenmei()) ;
			FieldTextArea naiyou =  new FieldTextArea(item.getValue().getNaiyou());
			switch(item.getKey()) {
				case English:
					dataBean.setStrKenmeiEn(kenmei);
					dataBean.setStrSousinNaiyouEn(naiyou);
					break;
				case SimplifiedChinese:
					dataBean.setStrKenmeiZhCn(kenmei);
					dataBean.setStrSousinNaiyouZhCn(naiyou);
					break;
				case TraditionalChinese:
					dataBean.setStrKenmeiZhHant(kenmei);
					dataBean.setStrSousinNaiyouZhHant(naiyou);
					break;
				default:
					JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "想定外の言語コード: " + item.getKey().getCode());
			}
		}
	}
	/**
	 * アウトバウンド内容をBeanの各言語毎に設定する（hidden用）
	 * @param dataBean
	 * @param map 言語毎のアウトバウンド送信内容
	 */
	public void setInboundOutboundInfoHidden(JOAL220101Bean dataBean, Map<Language, JOALMailTemplate> map) {
		for (Entry<Language, JOALMailTemplate> item: map.entrySet()) {
			FieldString kenmei = new FieldString(item.getValue().getKenmei()) ;
			FieldTextArea naiyou =  new FieldTextArea(item.getValue().getNaiyou());
			switch(item.getKey()) {
				case English:
					dataBean.setStrhidKenmeiEn(kenmei);
					dataBean.setStrhidSousinNaiyouEn(naiyou);
					break;
				case SimplifiedChinese:
					dataBean.setStrhidKenmeiZhCn(kenmei);
					dataBean.setStrhidSousinNaiyouZhCn(naiyou);
					break;
				case TraditionalChinese:
					dataBean.setStrhidKenmeiZhHant(kenmei);
					dataBean.setStrhidSousinNaiyouZhHant(naiyou);
					break;
				default:
					JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "想定外の言語コード: " + item.getKey().getCode());
			}
		}
	}
	/**
	 * インバウンド向けの対象件数を取得する
	 * @param id アウトバウンドID
	 * @return 言語コード毎の対象件数
	 * @throws Exception
	 */
	public Map<Language, Integer> getInboundTargetCount(String id) throws Exception {
		Map<Language, Integer> result = new HashMap<>();
		JOFDBAccesser dba = new JOFDBAccesser();
		try {
			ArrayList condition = new ArrayList(1);
			condition.add(id);

			ResultSet rs = dba.getResultSetBySelect("SLQ4556", condition);
			while (rs.next()) {
				Language lang = Language.get(rs.getString(1));
				if (lang == null) {
					throw new JOFGyomuRuntimeException(new Exception(), "ML0410E", JCMMessageManager.getMessage("ML0410E"));
				}

				result.put(lang, rs.getInt(2));
			}
			return result;
		}finally {
			dba.close();
		}
	}
	/**
	 * ＬＴアウトバウンド送信内容ＩＢを登録する
	 * @param id アウトバウンドID
	 * @param channelDiv PC/携帯/TEL区分
	 * @param dataBean
	 * @throws JOFFrameworkRuntimeException
	 * @throws JOFGyomuException
	 */
	public void insertInboundOutbound(String id, String channelDiv, JOAL220101Bean dataBean) throws JOFFrameworkRuntimeException, JOFGyomuException {
		JOFDBAccesser dba = new JOFDBAccesser();
		JCMCheckUtil checkUtil = new JCMCheckUtil();
		try {
			// 設定対象の言語
			List<Language> targetLanguages = new ArrayList(Arrays.asList(
					Language.English
					, Language.SimplifiedChinese
					, Language.TraditionalChinese
			));

			for (int i = 0; i < targetLanguages.size(); i++) {
				// 件名/内容が両方入力済か
				// 未入力の場合はその言語はスキップ
				boolean isInput = false;

				ArrayList condition = new ArrayList(11);
				condition.add(id);
				condition.add(channelDiv);
				switch(targetLanguages.get(i)) {
					case English:
						condition.add(Language.English.getCode());
						condition.add(dataBean.getStrKenmeiEn().getText());
						condition.add(dataBean.getStrSousinNaiyouEn().getText());
						isInput = checkUtil.isCheckExist(dataBean.getStrKenmeiEn().getText()) && checkUtil.isCheckExist(dataBean.getStrSousinNaiyouEn().getText());
						break;
					case SimplifiedChinese:
						condition.add(Language.SimplifiedChinese.getCode());
						condition.add(dataBean.getStrKenmeiZhCn().getText());
						condition.add(dataBean.getStrSousinNaiyouZhCn().getText());
						isInput = checkUtil.isCheckExist(dataBean.getStrKenmeiZhCn().getText()) && checkUtil.isCheckExist(dataBean.getStrSousinNaiyouZhCn().getText());
						break;
					case TraditionalChinese:
						condition.add(Language.TraditionalChinese.getCode());
						condition.add(dataBean.getStrKenmeiZhHant().getText());
						condition.add(dataBean.getStrSousinNaiyouZhHant().getText());
						isInput = checkUtil.isCheckExist(dataBean.getStrKenmeiZhHant().getText()) && checkUtil.isCheckExist(dataBean.getStrSousinNaiyouZhHant().getText());
						break;
				}
				condition.add(JOFUtil.getRegUserID());
				condition.add(CLASS_NAME);
				condition.add(JOFUtil.getConsoleID());
				condition.add(JOFUtil.getRegUserID());
				condition.add(CLASS_NAME);
				condition.add(JOFUtil.getConsoleID());

				if (isInput) {
					dba.execute("SLQ4495", condition);
				}
			}
		}finally {
			dba.close();
		}
	}
	/**
	 * ＬＴアウトバウンド送信内容ＩＢを削除する
	 * @param id アウトバウンドID
	 * @throws JOFFrameworkRuntimeException
	 * @throws JOFGyomuException
	 */
	public void deleteInboundOutbound(String id) throws JOFFrameworkRuntimeException, JOFGyomuException {
		JOFDBAccesser dba = new JOFDBAccesser();
		ArrayList condition = new ArrayList(1);
		condition.add(id);

		try {
			dba.execute("SLQ4496", condition);
		} finally {
			dba.close();
		}
	}
	/**
	 * オーダー言語の文章が入っていないが（英語で）送信してよいかの確認必要性
	 * @param dataBean
	 * @return
	 */
	public boolean requireConfirm(JOAL220101Bean dataBean) {
		boolean result = false;
		if (!dataBean.isToShowLanguageSelect()) {
			return false;
		}

		JCMCheckUtil util = new JCMCheckUtil();
		Map<Language, Integer> inboundCount = dataBean.getInboundTargetCount();
		JCMLog.debugLog(JCMLQConstants.DEBUG_LOG_LEVEL, "言語毎対象数: " + inboundCount);
		// 各言語未入力だが、対象アリの場合にtrueにする
		for (Entry<Language, Integer> item: inboundCount.entrySet()) {
			switch (item.getKey()) {
				case SimplifiedChinese:
					if (!util.isCheckExist(dataBean.getStrKenmeiZhCn().getText()) && item.getValue() > 0) {
						result = true;
					}
					break;
				case TraditionalChinese:
					if (!util.isCheckExist(dataBean.getStrKenmeiZhHant().getText()) && item.getValue() > 0) {
						result = true;
					}
					break;
				default:
					break;
			}
		}
		return result;
	}

	/**
	 * 件名・送信内容をクリアする
	 * @param dataBean
	 */
	public void clearMailContents(JOAL220101Bean dataBean) {
		// 日本語
		dataBean.setStrKenmei(new FieldString(""));
		dataBean.setStrKenmeiKeitai(new FieldString(""));
		dataBean.setStrSousinNaiyou(new FieldTextArea(""));
		dataBean.setStrSousinNaiyokeitai(new FieldTextArea(""));
		// インバウンド用
		dataBean.setStrKenmeiEn(new FieldString(""));
		dataBean.setStrSousinNaiyouEn(new FieldTextArea(""));
		dataBean.setStrKenmeiZhCn(new FieldString(""));
		dataBean.setStrSousinNaiyouZhCn(new FieldTextArea(""));
		dataBean.setStrKenmeiZhHant(new FieldString(""));
		dataBean.setStrSousinNaiyouZhHant(new FieldTextArea(""));
	}

	/**
	 * 言語毎に送信対象件数を設定する
	 * @param dataBean
	 */
	public void setLanguageTargetCount(JOAL220101Bean dataBean) {
		Integer sum = Integer.parseInt(dataBean.getStrZentaiNum());
		Map<Language, Integer> inboundCount = dataBean.getInboundTargetCount();

		// 英語
		Integer countEn = Optional.ofNullable(inboundCount.get(Language.English)).orElse(0);
		dataBean.setCountEn(countEn);
		// 簡体字
		Integer countZhchs = Optional.ofNullable(inboundCount.get(Language.SimplifiedChinese)).orElse(0);
		dataBean.setCountZhchs(countZhchs);
		// 繁体字
		Integer countZhcht = Optional.ofNullable(inboundCount.get(Language.TraditionalChinese)).orElse(0);
		dataBean.setCountZhcht(countZhcht);

		// 日本語
		// 全体からインバウンド分をマイナス
		Integer count = sum
				- countEn
				- countZhchs
				- countZhcht
		;
		dataBean.setCountJa(count);
	}

	/**
	 * 抽出条件の言語選択ボックスの中身を設定する
	 * @param dataBean
	 */
	public void setComboLanguage(JOAL220101Bean dataBean) {
		ComboBox combo = new ComboBox();
		ListBox list = new ListBox();
		combo.insert(0, "", "");
		// Listは0番をALLという意味で扱うの止めたいため、誤って選択することを危惧して表示しない
		// 英語
		combo.insert(1, Language.English.getCode(), "英語");
		list.insert(0, Language.English.getCode(), "英語");
		// 簡体字
		combo.insert(2, Language.SimplifiedChinese.getCode(), "簡体字");
		list.insert(1, Language.SimplifiedChinese.getCode(), "簡体字");
		// 繁体字
		combo.insert(3, Language.TraditionalChinese.getCode(), "繁体字");
		list.insert(2, Language.TraditionalChinese.getCode(), "繁体字");

		dataBean.setCmbLanguage(combo);
		dataBean.setLstLanguage(list);
	}

	/**
	 * 対象件数が1件以上の言語を選択状態にする
	 * @param dataBean
	 */
	public void setSelectedLanguage(JOAL220101Bean dataBean) {
		int num = dataBean.getCountJa();
		if (num > 0) {
			// 日本語送信がある場合、言語選択してないものと見做す
			dataBean.setSingleSelectableLanguage("1");
			return;
		}

		Map<Language, Integer> inboundCount = dataBean.getInboundTargetCount();
		String code = "";
		for (Entry<Language, Integer> item: inboundCount.entrySet()) {
			Integer count = Optional.ofNullable(item.getValue()).orElse(0);
			if (count > 0) {
				switch (item.getKey()) {
					case English:
						dataBean.getCmbLanguage().select(Language.English.getCode());
						code += Language.English.getCode() + ",";
						break;
					case SimplifiedChinese:
						dataBean.getCmbLanguage().select(Language.SimplifiedChinese.getCode());
						code += Language.SimplifiedChinese.getCode() + ",";
						break;
					case TraditionalChinese:
						dataBean.getCmbLanguage().select(Language.TraditionalChinese.getCode());
						code += Language.TraditionalChinese.getCode() + ",";
						break;
					default:
						break;
				}
			}
		}
		// 単数/複数選択のフラグ
		String[] selectedLanguages = code.split(",");
		if (selectedLanguages != null && selectedLanguages.length > 1) {
			dataBean.setSingleSelectableLanguage("0");
			dataBean.getLstLanguage().select(selectedLanguages);
			dataBean.getCmbLanguage().select(""); // 複数選択されている場合は、単数の方は初期値設定
		} else {
			dataBean.setSingleSelectableLanguage("1");
		}
	}
}
