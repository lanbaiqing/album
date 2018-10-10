使用方法
----------------------------------------
继承 PickActivity 或者 PickFragment
并实现方法 onPickCamera 和 onPickAlbums
其中onPickCamera为选择拍照后回调，onPickAlbums则是相册回调

setMax方法设置最多能选择多少张图片

setList方法设置已选中的图片

showPick方法显示拍照和相册对话框

openCamera方法直接打开相机拍照(无对话框)

openAlbums方法直接打开相册选择图片（无对话框）

----------------------------------------
AlbumPreview 相册预览使用【本地，网络图片都可以】

Intent intent = new Intent(this, AlbumPreview.class);

intent.putStringArrayListExtra("list", 你所需要预览的图片列表);

startActivity(intent);

