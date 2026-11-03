import AppKit
import Foundation
struct Asset:Decodable {let name:String;let source:String;let title:String;let subtitle:String;let eyebrow:String;let crop:[CGFloat]}
let root=URL(fileURLWithPath:FileManager.default.currentDirectoryPath)
let assets=try JSONDecoder().decode([Asset].self,from:Data(contentsOf:root.appendingPathComponent("store-assets/manifest.json")))
func color(_ hex:UInt32,_ a:CGFloat=1)->NSColor {NSColor(calibratedRed:CGFloat((hex>>16)&255)/255,green:CGFloat((hex>>8)&255)/255,blue:CGFloat(hex&255)/255,alpha:a)}
func box(_ rect:NSRect,_ c:NSColor,_ radius:CGFloat=0){c.setFill();NSBezierPath(roundedRect:rect,xRadius:radius,yRadius:radius).fill()}
func text(_ s:String,_ rect:NSRect,_ size:CGFloat,_ weight:NSFont.Weight,_ c:NSColor,_ align:NSTextAlignment = .left){
 let para=NSMutableParagraphStyle();para.alignment=align;para.lineSpacing=4
 (s as NSString).draw(in:rect,withAttributes:[.font:NSFont.systemFont(ofSize:size,weight:weight),.foregroundColor:c,.paragraphStyle:para])
}
func screen(_ asset:Asset,_ rect:NSRect){
 let im=NSImage(contentsOfFile:asset.source)!;let crop=asset.crop
 NSGraphicsContext.saveGraphicsState()
 NSBezierPath(roundedRect:rect,xRadius:30,yRadius:30).addClip()
 im.draw(in:rect,from:NSRect(x:crop[0],y:im.size.height-crop[1]-crop[3],width:crop[2],height:crop[3]),operation:.sourceOver,fraction:1,respectFlipped:true,hints:[.interpolation:NSImageInterpolation.high])
 NSGraphicsContext.restoreGraphicsState()
}
func render(_ name:String,_ width:Int,_ height:Int,_ draw:()->Void)throws{
 let bitmap=NSBitmapImageRep(bitmapDataPlanes:nil,pixelsWide:width,pixelsHigh:height,bitsPerSample:8,samplesPerPixel:4,hasAlpha:true,isPlanar:false,colorSpaceName:.deviceRGB,bytesPerRow:0,bitsPerPixel:0)!
 let ctx=NSGraphicsContext(bitmapImageRep:bitmap)!.cgContext;ctx.translateBy(x:0,y:CGFloat(height));ctx.scaleBy(x:1,y:-1)
 NSGraphicsContext.saveGraphicsState();NSGraphicsContext.current=NSGraphicsContext(cgContext:ctx,flipped:true)
 draw();NSGraphicsContext.restoreGraphicsState()
 try bitmap.representation(using:.png,properties:[:])!.write(to:root.appendingPathComponent(name))
}
for (i,a) in assets.enumerated(){try render("store-assets/phone/\(a.name).png",1080,1920){
 NSGradient(colors:[color(0x101E42),color(0x153D64),color(0x176A78)])!.draw(in:NSRect(x:0,y:0,width:1080,height:1920),angle:60)
 box(NSRect(x:770,y:1260,width:580,height:580),color(0x47DED0,0.09),290)
 text("ExamAI",NSRect(x:72,y:54,width:500,height:58),39,.bold,.white)
 text(String(format:"%02d / 08",i+1),NSRect(x:780,y:62,width:228,height:40),25,.medium,color(0xA3C4DE),.right)
 text(a.eyebrow,NSRect(x:72,y:142,width:940,height:48),25,.semibold,color(0x6DE1D2))
 text(a.title,NSRect(x:68,y:200,width:944,height:210),78,.bold,.white)
 text(a.subtitle,NSRect(x:72,y:402,width:930,height:105),32,.regular,color(0xD5E8F5))
 let aspect=a.crop[3]/a.crop[2]
 let w:CGFloat=min(850,1230/aspect);let h=w*aspect;let x=(1080-w)/2;let y:CGFloat=550
 box(NSRect(x:x-14,y:y-14,width:w+28,height:h+28),color(0x050C20),43)
 box(NSRect(x:x-4,y:y-4,width:w+8,height:h+8),color(0x3D5877),34)
 screen(a,NSRect(x:x,y:y,width:w,height:h))
 if h<1060 {
  let labels=i==5 ? ["Find a topic", "Save a summary", "Read offline"] : (i==3 ? ["Set a daily question goal", "Add your exam date", "Create optional reminders"] : ["Missed + saved questions", "Tap to reveal answers", "Again or Got it"])
  for (j,label) in labels.enumerated(){let yy=y+h+65+CGFloat(j)*72;box(NSRect(x:112,y:yy+5,width:12,height:12),color(0x6DE1D2),6);text(label,NSRect(x:150,y:yy-4,width:800,height:50),29,.medium,color(0xE1EFF7))}
 }
 let note=i==7 ? "Interface languages vary. Questions and study content remain in English." : "Actual app screen details • Example study data"
 text(note,NSRect(x:65,y:1810,width:950,height:64),22,.regular,color(0xB4D1E3),.center)
}}
try render("store-assets/feature_graphic.png",1024,500){
 NSGradient(colors:[color(0x101E42),color(0x176A78)])!.draw(in:NSRect(x:0,y:0,width:1024,height:500),angle:30)
 text("ExamAI",NSRect(x:50,y:42,width:460,height:65),46,.bold,.white)
 text("Your next chapter\nstarts with practice.",NSRect(x:48,y:135,width:650,height:190),53,.bold,.white)
 text("Practice • Plan • Revise",NSRect(x:50,y:346,width:600,height:55),29,.medium,color(0x6DE1D2))
 text("A study companion for your exam journey",NSRect(x:50,y:422,width:650,height:40),21,.regular,color(0xD5E8F5))
 let a=assets[4];box(NSRect(x:712,y:108,width:270,height:285),color(0x071227),25)
 screen(a,NSRect(x:720,y:125,width:254,height:249))
}
