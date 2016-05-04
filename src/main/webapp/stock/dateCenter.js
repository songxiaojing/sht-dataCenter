/**
 * Created by baiyanwei on 5/4/16.
 */

//关闭下拉菜单
function addEventListenerForTopMenu() {
    try {
        //增加关闭菜单事件给主FRAME
        document.getElementById("content").contentWindow.document.addEventListener("click", function () {
            $(".dropdown").each(function () {
                //$(this).removeClass("open");
                $(this).trigger($.Event('hide.bs.dropdown', {}));
                $(this).removeClass('open').trigger('hidden.bs.dropdown', {});
            });
        }, false);
    } catch (error) {
    }

}
