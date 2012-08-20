package com.suppermm;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import weibo4j.model.Status;
import weibo4j.model.User;

import com.itextpdf.text.Anchor;
import com.itextpdf.text.Annotation;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.html.WebColors;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.suppermm.util.ImageDBUtility;
import com.suppermm.util.WeiboImageUtil;

public class WeiboPageCreator {
	Font unicodefont;
	Font FontChinese;
	Font FontChinese1;
	Font FontChinese2;
	Font FontChinese3;
	Font unicodeurlfont;
	Font graylinkfont;
	Font retweetedtextfont;
	private ImageDBUtility idb;
	public WeiboPageCreator(){
		// pdf文档中中文字体的设置，注意一定要添加iTextAsian.jar包 
		
		idb=new ImageDBUtility(false);//初始化数据库访问类
		BaseFont unicode;
		try {
			InputStream is = getResourceStream("/font/simkai.ttf");
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte buf[] = new byte[1024];
			while (true) {
			int size = is.read(buf);
			if (size < 0)
			break;
			out.write(buf, 0, size);
			}
			is.close();
			buf = out.toByteArray();
			System.out.println("being font");
			unicode = BaseFont.createFont("simkai.ttf", BaseFont.IDENTITY_H,BaseFont.EMBEDDED, true, buf, null);
			
//			unicode = BaseFont.createFont("/SIMKAI.TTF", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
			System.out.println("finish font");
			unicodefont = new Font(unicode, 7, Font.NORMAL);// 加入document：
			System.out.println("new font");
			unicodeurlfont = new Font(unicode, 7, Font.UNDERLINE,new BaseColor(0,0,255));// 加入document：
			graylinkfont = new Font(unicode, 7, Font.UNDERLINE,new BaseColor(100,100,100));
			retweetedtextfont = new Font(unicode, 7, Font.NORMAL,new BaseColor(100,100,100));

		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		WeiboPageCreator pdfc = new WeiboPageCreator();

		SaveAllweiboToLocal saver=new SaveAllweiboToLocal();
		
		HashMap<Long, Status> weibocontents=saver.ReadAllWeibobyUid("2144194177", LocalConfig.access_token);
//		pdfc.test();
		User user=FetchWeiboInfo.getUserbyUID("2144194177");
		pdfc.creatPDF(user, weibocontents,"d:\\MyIText.pdf");
	}

	public void creatPDF(User user,
			HashMap<Long, Status> weibocontents,String targetFilename) {
		// 步骤 1: 创建一个document对象，大小为A4，上下左右边距都为36
		//Document document = new Document(PageSize.A4, 35, 35, 20, 20);
		try {
			BaseColor bgcolor = WebColors.getRGBColor("#C8BFE7");
			Rectangle pageSize = new Rectangle(PageSize.A4);
		    pageSize.setBackgroundColor(bgcolor);
		    Document document = new Document(pageSize);
		    
			// 步骤 2:
			// 我们为document创建一个监听，并把PDF流写到文件中
			PdfWriter.getInstance(document, new FileOutputStream(
					targetFilename));
			// 步骤 3:打开文档
			document.open();
			PdfPTable userTable=createUserTable(user);
			// 把定义好的表格增加到文档中
			document.add(userTable);
			PdfPTable statusTable=createOuterStatusTable(weibocontents);
			document.add(statusTable);
			//document.add(statusTable);
			// 步骤 5:关闭文档
			document.close();
		} catch (Exception ex) {
			idb.closeConnection();
			ex.printStackTrace();
		}
	}
	public PdfPTable createUserTable(User user){
		String baseurl="http://weibo.com/u/";		
		try{			
		
			PdfPTable userTable = new PdfPTable(4);
			userTable.setTotalWidth(new float[]{20, 100, 300, 20});
			userTable.setLockedWidth(true);			
			userTable.setHorizontalAlignment(Element.ALIGN_CENTER);
			
			//第一列留空白
			PdfPCell cell = new PdfPCell(new Paragraph("    ", unicodefont));
			cell.setRowspan(6);
			cell.setBackgroundColor(BaseColor.WHITE);
			cell.setBorder(Rectangle.NO_BORDER);
			userTable.addCell(cell);
			
			//第二列第一行留空白
			cell = new PdfPCell(new Paragraph("    ", unicodefont));			
			cell.setBackgroundColor(BaseColor.WHITE);
			cell.setBorder(Rectangle.NO_BORDER);
			userTable.addCell(cell);
			
			//第三列第一行留空白
			userTable.addCell(cell);
			
			//第四列留空白
			cell = new PdfPCell(new Paragraph("    ", unicodefont));
			cell.setBackgroundColor(BaseColor.WHITE);
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setRowspan(6);
			userTable.addCell(cell);
			
			//第二列第二行至第五行显示图像
			Image img;
			try{
			byte[] input=idb.getPhotoImageBlob(user.getId());
			img=Image.getInstance(input);
			//img = Image.getInstance("flower.jpg");
			}catch(Exception ex){
				img = Image.getInstance(getImageByte("default.jpg"));
				ex.printStackTrace();
			}
			img.scaleToFit(120, 120);
			img.setAlignment(Element.ALIGN_RIGHT);
			cell = new PdfPCell(img);
			cell.setRowspan(4);
			cell.setBackgroundColor(BaseColor.WHITE);
			cell.setBorder(Rectangle.NO_BORDER);
			userTable.addCell(cell);
	
			//第三列第二行显示文字
			cell = new PdfPCell(new Paragraph(user.getName(), unicodefont));
			cell.setFixedHeight(15);
			cell.setPaddingLeft(15);
			cell.setBackgroundColor(BaseColor.WHITE);
			cell.setBorder(Rectangle.NO_BORDER);
			userTable.addCell(cell);		
			
			//第三列第三行显示文字
			Paragraph p= new Paragraph(baseurl+user.getId(),unicodeurlfont);
			cell = new PdfPCell(p);
			cell.setFixedHeight(15);
			cell.setPaddingLeft(15);
			cell.setBackgroundColor(BaseColor.WHITE);
			cell.setBorder(Rectangle.NO_BORDER);
			userTable.addCell(cell);
			
			//第三列第四行显示文字
			cell = new PdfPCell(new Paragraph(user.getDescription(), unicodefont));
			cell.setFixedHeight(15);
			cell.setPaddingLeft(15);
			cell.setBackgroundColor(BaseColor.WHITE);
			cell.setBorder(Rectangle.NO_BORDER);
			userTable.addCell(cell);
			
			//第三列第五行空白
			cell = new PdfPCell(new Paragraph("    ", unicodefont));
			cell.setBackgroundColor(BaseColor.WHITE);			
			cell.setBorder(Rectangle.NO_BORDER);
			userTable.addCell(cell);
			
			//第二列第六行留空白					
			userTable.addCell(cell);
			
			//第三列第六行空白
			userTable.addCell(cell);			
			
			
		return userTable;	
			}catch(Exception ex){
				ex.printStackTrace();
			}
		return null;
	}
	
	private byte[] getImageByte(String filename) {
		byte buf[]=null;
		try {
			InputStream is = getResourceStream("/font/"+filename);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			buf = new byte[1024];
			while (true) {
			int size = is.read(buf);
			if (size < 0)
			break;
			out.write(buf, 0, size);
			}
			is.close();
			buf = out.toByteArray();
			}catch(Exception ex){
		
			}
			return buf;
		}

	public PdfPTable createOuterStatusTable(HashMap<Long, Status> weibocontents){
		PdfPTable outerTable = null;
		try{
			//三列表格，左右两列留白
			outerTable = new PdfPTable(3);
			outerTable.setTotalWidth(new float[]{20, 400, 20});
			outerTable.setLockedWidth(true);		
			outerTable.setHorizontalAlignment(Element.ALIGN_CENTER);
			//第一列空白
			PdfPCell cell = new PdfPCell(new Paragraph("    ", unicodefont));
			cell.setBackgroundColor(BaseColor.WHITE);
			cell.setBorder(Rectangle.NO_BORDER);
			outerTable.addCell(cell);
			//第二列嵌表格
			cell = new PdfPCell();
			cell.addElement(createStatusTable(weibocontents));
			cell.setBackgroundColor(BaseColor.WHITE);
			cell.setBorder(Rectangle.NO_BORDER);
			outerTable.addCell(cell);
			//第三列空白
			cell = new PdfPCell(new Paragraph("    ", unicodefont));
			cell.setBackgroundColor(BaseColor.WHITE);
			cell.setBorder(Rectangle.NO_BORDER);
			outerTable.addCell(cell);			
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return outerTable;
	}
	
	
	public PdfPTable createStatusTable(HashMap<Long, Status> weibocontents){
		PdfPTable statusTable = null;
		try{
			statusTable = new PdfPTable(1);							
			statusTable.setTotalWidth(400);
			statusTable.setLockedWidth(true);			
			statusTable.setHorizontalAlignment(Element.ALIGN_CENTER);
			statusTable.setWidthPercentage(100);
			
		
				
			Object[] key_arr = weibocontents.keySet().toArray();
			Arrays.sort(key_arr,Collections.reverseOrder());
			BaseColor color = WebColors.getRGBColor("#C8BFE7");
			BaseColor lightbgcolor = WebColors.getRGBColor("#EEEEEE");
			BaseColor darkbgcolor = WebColors.getRGBColor("#CCCCCC");
			Paragraph statusText = null;
			PdfPCell cell = null;
			int count=0;
			for (Object key : key_arr) {
				count++;
	
				
				try{		
					//显示微博内容
					Status tempstatus=weibocontents.get(key);
					statusText = new Paragraph(tempstatus.getText(), unicodefont);
					cell = new PdfPCell(statusText);
					cell.setVerticalAlignment(Element.ALIGN_CENTER);
					cell.setPaddingLeft(10f);
					cell.setPaddingTop(5f);
					cell.setPaddingBottom(5f);
					if(count % 2 == 0){ cell.setBackgroundColor(lightbgcolor);cell.setBorderColor(lightbgcolor);}
					else{ cell.setBackgroundColor(darkbgcolor); cell.setBorderColor(darkbgcolor);}
					
					statusTable.addCell(cell);
					
					//如果有图片，显示小图片
					String imgUrl = tempstatus.getThumbnailPic(); 					
					if(imgUrl.contains("http")){				
						String bigimgUrl = "";
						if(tempstatus.getOriginalPic() != ""){
							bigimgUrl = tempstatus.getOriginalPic();	
							//System.out.println("count: "+count+tempstatus.getBmiddlePic()+"    "+tempstatus.getOriginalPic());
						}
						cell = showStatusImage(imgUrl, bigimgUrl);
						if(count % 2 == 0){ cell.setBackgroundColor(lightbgcolor);cell.setBorderColor(lightbgcolor);}
						else{ cell.setBackgroundColor(darkbgcolor); cell.setBorderColor(darkbgcolor);}
						cell.setPaddingLeft(10f);
						cell.setPaddingTop(5f);
						cell.setPaddingBottom(5f);
						statusTable.addCell(cell);											
					}
					
					String screenName = "";
					//如果是转发的微博，显示之
					if(tempstatus.getRetweetedStatus() != null){
						Status tempRetweetedStatus = tempstatus.getRetweetedStatus();
						if(tempRetweetedStatus.getUser() != null)
							screenName = tempRetweetedStatus.getUser().getScreenName();
						statusText = new Paragraph( screenName + ": " + tempRetweetedStatus.getText(), retweetedtextfont);						
						cell = new PdfPCell(statusText);
						cell.setVerticalAlignment(Element.ALIGN_CENTER);							
						cell.setPaddingLeft(15f);	
						cell.setPaddingTop(5f);
						if(count % 2 == 0){ cell.setBackgroundColor(lightbgcolor);}
						else{ cell.setBackgroundColor(darkbgcolor);}
						cell.setBorderColor(BaseColor.LIGHT_GRAY);
						cell.setBorder(Rectangle.NO_BORDER);
						cell.enableBorderSide(Rectangle.TOP);
						cell.enableBorderSide(Rectangle.LEFT);
						cell.enableBorderSide(Rectangle.RIGHT);
						statusTable.addCell(cell);
						
						//如果转发的微博里面有图片，显示小图片
						imgUrl = tempRetweetedStatus.getThumbnailPic(); 
						
						if(imgUrl.contains("http")){
							String bigimgUrl = "";
							if(tempRetweetedStatus.getOriginalPic() != ""){
								bigimgUrl = tempRetweetedStatus.getOriginalPic();
								//System.out.println("count: "+count+tempRetweetedStatus.getBmiddlePic()+"    "+tempRetweetedStatus.getOriginalPic());
							}
							cell = showStatusImage(imgUrl, bigimgUrl);
							cell.setPaddingLeft(15f);	
							cell.setPaddingTop(5f);
							cell.setPaddingBottom(5f);															
							if(count % 2 == 0){ cell.setBackgroundColor(lightbgcolor);}
							else{ cell.setBackgroundColor(darkbgcolor); }
							cell.setBorderColor(BaseColor.LIGHT_GRAY);
							cell.setBorder(Rectangle.NO_BORDER);
							cell.enableBorderSide(Rectangle.LEFT);
							cell.enableBorderSide(Rectangle.RIGHT);	
							statusTable.addCell(cell);
						}
						//显示转发时间						
						cell = showStatusPostTime(tempRetweetedStatus, retweetedtextfont);
						cell.setPaddingLeft(15f);			
						cell.setPaddingBottom(5f);
						if(count % 2 == 0){ cell.setBackgroundColor(lightbgcolor);}
						else{ cell.setBackgroundColor(darkbgcolor);}
						cell.setBorderColor(BaseColor.LIGHT_GRAY);
						cell.setBorder(Rectangle.NO_BORDER);
						cell.enableBorderSide(Rectangle.BOTTOM);
						cell.enableBorderSide(Rectangle.LEFT);
						cell.enableBorderSide(Rectangle.RIGHT);
						statusTable.addCell(cell);
						
					}
					
					//显示发布时间					
					cell = showStatusPostTime(tempstatus, unicodefont);					
					cell.setPaddingBottom(10f);
					cell.setPaddingLeft(10f);
					cell.setPaddingTop(5f);
					cell.setBorder(Rectangle.NO_BORDER);
					cell.enableBorderSide(Rectangle.BOTTOM);
					cell.enableBorderSide(Rectangle.LEFT);
					cell.enableBorderSide(Rectangle.RIGHT);
					if(count % 2 == 0){ cell.setBackgroundColor(lightbgcolor);cell.setBorderColor(lightbgcolor);}
					else{ cell.setBackgroundColor(darkbgcolor); cell.setBorderColor(darkbgcolor);} 
					statusTable.addCell(cell);
					
					}catch(Exception ex){
						ex.printStackTrace();
					}
				}
			}catch(Exception ex){
				ex.printStackTrace();
			}

		return statusTable;

	}

	private PdfPCell showStatusImage(String imgUrl, String bigimgurl) {	
		PdfPCell cell = null;
		Image img=null;
		try{
			String filename=WeiboImageUtil.getFilenameFromURL(imgUrl);
			byte[] input=idb.getPhotoImageBlob(filename);
			img = Image.getInstance(input);

		//	cell.setBackgroundColor(BaseColor.WHITE);
		//	cell.setBorder(Rectangle.NO_BORDER);
		}catch(Exception ex){
			try {
				img = Image.getInstance(getImageByte("default.jpg"));
			} catch (BadElementException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ex.printStackTrace();
		}finally{
			img.scaleToFit(90, 60);	
			//定义图片链接
			if(bigimgurl != "") img.setAnnotation(new Annotation(0,0,0,0, bigimgurl));
			cell = new PdfPCell(img);
		}
		return cell;	
	}
	
	private PdfPCell showStatusPostTime(Status status, Font textfont){
		Paragraph par = null;
		String fromUser = "";
		String linkUrl = "";
		if(status.getCreatedAt() != null)
			par = new Paragraph(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(status.getCreatedAt())+"   来自", textfont);
		else par = new Paragraph("");
		//System.out.println(tempRetweetedStatus.getCreatedAt());
		if(status.getSource() != null){
			fromUser = status.getSource().getName();
			linkUrl = status.getSource().getUrl();
		}
		Anchor fromLink = new Anchor(fromUser, textfont);
		fromLink.setReference(linkUrl);		
		par.add(fromLink);
		PdfPCell cell = new PdfPCell(par);
	//	cell.setBackgroundColor(BaseColor.WHITE);
	//	cell.setBorder(Rectangle.NO_BORDER);
	//	cell.setBorderWidth(0.5f);
	//	cell.setBorderColor(BaseColor.LIGHT_GRAY);
		return cell;
	}
	
	public PdfPTable createStatusInternalTable(Status status){
		PdfPTable statusTable=null;
		boolean fetchimage=false;
		
		try{
		// 创建三列表格，左右两列仍旧留白		
		statusTable = new PdfPTable(3);
		statusTable.setHorizontalAlignment(Element.ALIGN_CENTER);
		statusTable.setWidthPercentage(100);// table100%
		
		
		
		Paragraph titlep=new Paragraph(status.getText(), unicodefont);
		PdfPCell cell = new PdfPCell(titlep);
		cell.setVerticalAlignment(Element.ALIGN_CENTER);
		cell.setBorder(Rectangle.NO_BORDER);
		statusTable.addCell(cell);

		if(fetchimage&&status.getThumbnailPic().length()>5){
		Anchor anchor = new Anchor("Original Pic",unicodeurlfont);  
        anchor.setReference(status.getOriginalPic());  
        anchor.setName("Original Pic");  
//        titlep.add(anchor);
//		Image bmp=Image.getInstance(new URL(status.getThumbnailPic()));
//		bmp.scalePercent(50f);
//		cell.setFixedHeight(32);
//		cell = new PdfPCell(bmp);
//		
//		statusTable.addCell(cell);
		}
		
		if(status.getRetweetedStatus()!=null){
			titlep=new Paragraph(status.getRetweetedStatus().getText(), unicodefont);
			cell = new PdfPCell(titlep);
			cell.setVerticalAlignment(Element.ALIGN_CENTER);
			cell.setBorder(Rectangle.NO_BORDER);
			statusTable.addCell(cell);
			
			if(fetchimage&&status.getRetweetedStatus().getThumbnailPic().length()>5){
			Anchor anchor = new Anchor("Original Pic",unicodeurlfont);  
	        anchor.setReference(status.getRetweetedStatus().getOriginalPic());  
	        anchor.setName("Original Pic");  
//	        titlep.add(anchor);
			Image bmp=Image.getInstance(new URL(status.getRetweetedStatus().getThumbnailPic()));
			bmp.scalePercent(50f);
			cell.setFixedHeight(16);
			cell = new PdfPCell(bmp);
			cell.setBorder(Rectangle.NO_BORDER);
//			cell.addElement(anchor);
			statusTable.addCell(cell);
			}	
		
		}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return statusTable;

	}

	public static InputStream getResourceStream(String key) {
		InputStream is = null;
		// Try to use Context Class Loader to load the properties file.
		try {
		java.lang.reflect.Method getCCL =
		Thread.class.getMethod("getContextClassLoader", new
		Class[0]);
		if (getCCL != null) {
		ClassLoader contextClassLoader =

		(ClassLoader)getCCL.invoke(Thread.currentThread(),
		new Object[0]);
		is = contextClassLoader.getResourceAsStream(key);
		}
		} catch (Exception e) {}

		if (is == null) {
		is = BaseFont.class.getResourceAsStream(key);
		}
		return is;
		}
}