package org.oelerich.BBCodeParser;

public class SimpleParser extends BBCodeParser {

	public static String allNodes = "string, b, u, s, i, mod, spoiler, "
			+ "code, php, html, img, quote, url, list, table, size, "
			+ "color, left, center, right, intend, plain, font";

	// TODO:
	// email, user, media
	// Better spoilers

	public SimpleParser() {
		super();

		BBCodeTag b;

		b = new BBCodeTag();
		b.mTag = "b";
		b.mDescription = "Bold";
		b.allow(allNodes);
		b.html("<b>{0}</b>");
		b.mInvalidStartRecovery = BBCodeTag.RECOVERY_CLOSE;
		b.mInvalidEndRecovery = BBCodeTag.RECOVERY_REOPEN;
		registerTag(b);

		b = new BBCodeTag();
		b.mTag = "u";
		b.mDescription = "Underline";
		b.allow(allNodes);
		b.html("<u>{0}</u>");
		b.mInvalidStartRecovery = BBCodeTag.RECOVERY_CLOSE;
		b.mInvalidEndRecovery = BBCodeTag.RECOVERY_REOPEN;
		registerTag(b);

		b = new BBCodeTag();
		b.mTag = "s";
		b.mDescription = "Strike";
		b.allow(allNodes);
		b.html("<strike>{0}</strike>");
		b.mInvalidStartRecovery = BBCodeTag.RECOVERY_CLOSE;
		b.mInvalidEndRecovery = BBCodeTag.RECOVERY_REOPEN;
		registerTag(b);

		b = new BBCodeTag();
		b.mTag = "i";
		b.mDescription = "Italic";
		b.allow(allNodes);
		b.html("<i>{0}</i>");
		b.mInvalidStartRecovery = BBCodeTag.RECOVERY_CLOSE;
		b.mInvalidEndRecovery = BBCodeTag.RECOVERY_REOPEN;
		registerTag(b);

		b = new BBCodeTag();
		b.mTag = "spoiler";
		b.mDescription = "Spoiler";
		b.allow(allNodes);
		b.html("<pre>{0}<pre>");
		b.mInvalidStartRecovery = BBCodeTag.RECOVERY_CLOSE;
		b.mInvalidEndRecovery = BBCodeTag.RECOVERY_REOPEN;
		registerTag(b);

		b = new BBCodeTag();
		b.mTag = "code";
		b.mDescription = "Code";
		b.allow(allNodes);
		b.html("<pre style='background:#85D4FF;'>{0}</pre>");
		b.mInvalidStartRecovery = BBCodeTag.RECOVERY_CLOSE;
		b.mInvalidEndRecovery = BBCodeTag.RECOVERY_REOPEN;
		registerTag(b);

		b = new BBCodeTag();
		b.mTag = "html";
		b.mDescription = "HTML Code";
		b.allow(allNodes);
		b.html("<pre>{0}</pre>");
		b.mInvalidStartRecovery = BBCodeTag.RECOVERY_CLOSE;
		b.mInvalidEndRecovery = BBCodeTag.RECOVERY_REOPEN;
		registerTag(b);

		b = new BBCodeTag();
		b.mTag = "php";
		b.mDescription = "PHP Code";
		b.allow(allNodes);
		b.html("<pre>{0}</pre>");
		b.mInvalidStartRecovery = BBCodeTag.RECOVERY_CLOSE;
		b.mInvalidEndRecovery = BBCodeTag.RECOVERY_REOPEN;
		registerTag(b);

		b = new BBCodeTag();
		b.mTag = "mod";
		b.mDescription = "Highlight";
		b.allow(allNodes);
		b.html("<font color=\"red\">{0}</font>");
		b.mInvalidStartRecovery = BBCodeTag.RECOVERY_CLOSE;
		b.mInvalidEndRecovery = BBCodeTag.RECOVERY_REOPEN;
		registerTag(b);

		b = new BBCodeTag();
		b.mTag = "list";
		b.mDescription = "List";
		b.allow("*");
		b.html(0, "{0}");
		b.html(1, "{0}");
		b.mInvalidRecoveryAddTag = "*";
		b.mInvalidStartRecovery = BBCodeTag.RECOVERY_ADD;
		b.mInvalidStringRecovery = BBCodeTag.RECOVERY_ADD;
		b.mInvalidEndRecovery = BBCodeTag.RECOVERY_CLOSE;
		registerTag(b);

		b = new BBCodeTag();
		b.mTag = "quote";
		b.mDescription = "Quote";
		b.allow(allNodes);
		b.html(0, "<pre style='background:#00A6FF'>{0}</pre>");
		// b.html(3, "<pre style='background:#00A6FF'><b>{3}</b><br/>{0}");
		b.mInvalidStartRecovery = BBCodeTag.RECOVERY_CLOSE;
		b.mInvalidEndRecovery = BBCodeTag.RECOVERY_REOPEN;
		registerTag(b);

		b = new BBCodeTag();
		b.mTag = "*";
		b.mDescription = "Listitem";
		b.allow(allNodes);
		b.html("  * {0}");
		b.mInvalidStartRecovery = BBCodeTag.RECOVERY_CLOSE;
		b.mInvalidEndRecovery = BBCodeTag.RECOVERY_CLOSE;
		registerTag(b);

		b = new BBCodeTag();
		b.mTag = "url";
		b.mDescription = "Link";
		b.allow(allNodes);
		b.html(0, "<a href={0}>{0}</a>");
		b.html(1, "<a href={1}>{0}</a>");
		b.mInvalidStartRecovery = BBCodeTag.RECOVERY_STRING;
		b.mInvalidEndRecovery = BBCodeTag.RECOVERY_STRING;
		registerTag(b);

		b = new BBCodeTag();
		b.mTag = "img";
		b.mDescription = "Image";
		b.allow("string");
		b.html("<img src={0} />");
		b.mInvalidStartRecovery = BBCodeTag.RECOVERY_CLOSE;
		b.mInvalidEndRecovery = BBCodeTag.RECOVERY_REOPEN;
		registerTag(b);

		b = new BBCodeTag();
		b.mTag = "table";
		b.mDescription = "Table";
		b.allow("--");
		b.html("<table></table>");
		b.mInvalidRecoveryAddTag = "--";
		b.mInvalidStartRecovery = BBCodeTag.RECOVERY_ADD;
		b.mInvalidStringRecovery = BBCodeTag.RECOVERY_ADD;
		b.mInvalidEndRecovery = BBCodeTag.RECOVERY_CLOSE;
		registerTag(b);

		b = new BBCodeTag();
		b.mTag = "--";
		b.mDescription = "TableRow";
		b.allow("||");
		b.html("<tr></tr>");
		b.mInvalidRecoveryAddTag = "||";
		b.mInvalidStartRecovery = BBCodeTag.RECOVERY_ADD;
		b.mInvalidStringRecovery = BBCodeTag.RECOVERY_ADD;
		b.mInvalidEndRecovery = BBCodeTag.RECOVERY_CLOSE;
		registerTag(b);

		b = new BBCodeTag();
		b.mTag = "||";
		b.mDescription = "TableCol";
		b.allow(allNodes);
		b.html("<td></td>");
		b.mInvalidStartRecovery = BBCodeTag.RECOVERY_CLOSE;
		b.mInvalidEndRecovery = BBCodeTag.RECOVERY_CLOSE;
		registerTag(b);

		b = new SizeTag();
		registerTag(b);

		b = new BBCodeTag();
		b.mTag = "font";
		b.mDescription = "Font";
		b.allow(allNodes);
		b.html(1, "<font color=\"{1}\">{0}</font>");
		b.mInvalidStartRecovery = BBCodeTag.RECOVERY_CLOSE;
		b.mInvalidEndRecovery = BBCodeTag.RECOVERY_REOPEN;
		registerTag(b);

		b = new BBCodeTag();
		b.mTag = "color";
		b.mDescription = "Color";
		b.allow(allNodes);
		b.html(1, "<span style=\"color:{1}\">{0}</span>");
		// b.html(3, "<span style=\"color:{1},{2},{3}\">{0}</span>");
		b.mInvalidStartRecovery = BBCodeTag.RECOVERY_CLOSE;
		b.mInvalidEndRecovery = BBCodeTag.RECOVERY_REOPEN;
		registerTag(b);

		b = new BBCodeTag();
		b.mTag = "left";
		b.mDescription = "Left";
		b.allow(allNodes);
		b.html("<div style=\"text-align:left\">{0}</div>");
		b.mInvalidStartRecovery = BBCodeTag.RECOVERY_CLOSE;
		b.mInvalidEndRecovery = BBCodeTag.RECOVERY_REOPEN;
		registerTag(b);

		b = new BBCodeTag();
		b.mTag = "center";
		b.mDescription = "Center";
		b.allow(allNodes);
		b.html("<div style=\"text-align:center\">{0}</div>");
		b.mInvalidStartRecovery = BBCodeTag.RECOVERY_CLOSE;
		b.mInvalidEndRecovery = BBCodeTag.RECOVERY_REOPEN;
		registerTag(b);

		b = new BBCodeTag();
		b.mTag = "right";
		b.mDescription = "Right";
		b.allow(allNodes);
		b.html("<div style=\"text-align:right\">{0}</div>");
		b.mInvalidStartRecovery = BBCodeTag.RECOVERY_CLOSE;
		b.mInvalidEndRecovery = BBCodeTag.RECOVERY_REOPEN;
		registerTag(b);

		b = new BBCodeTag();
		b.mTag = "indent";
		b.mDescription = "Indent";
		b.allow(allNodes);
		b.html("<div style=\"padding-left:30px;\">{0}</div>");
		b.mInvalidStartRecovery = BBCodeTag.RECOVERY_CLOSE;
		b.mInvalidEndRecovery = BBCodeTag.RECOVERY_REOPEN;
		registerTag(b);

		b = new BBCodeTag();
		b.mTag = "plain";
		b.mDescription = "Plain text";
		b.allow("");
		b.html("");
		b.mInvalidStartRecovery = BBCodeTag.RECOVERY_CLOSE;
		b.mInvalidEndRecovery = BBCodeTag.RECOVERY_REOPEN;
		registerTag(b);
	}

	public static class SizeTag extends BBCodeTag {
		public SizeTag() {
			mTag = "size";
			mDescription = "Size";
			allow(allNodes);
			html(1, "<span style=\"font-size:{1}\">{0}</span>");
			mInvalidStartRecovery = BBCodeTag.RECOVERY_CLOSE;
			mInvalidEndRecovery = BBCodeTag.RECOVERY_REOPEN;
		}

		@Override
		public String replaceArgument(int id, String what) {
			if (id == 1) {
				if (what.equals("1")) {
					return "9px";
				} else if (what.equals("2")) {
					return "10px";
				} else if (what.equals("3")) {
					return "12px";
				} else if (what.equals("4")) {
					return "15px";
				} else if (what.equals("5")) {
					return "18px";
				} else if (what.equals("6")) {
					return "22px";
				} else if (what.equals("7")) {
					return "26px";
				}
			}
			return super.replaceArgument(id, what);
		}
	}
}
