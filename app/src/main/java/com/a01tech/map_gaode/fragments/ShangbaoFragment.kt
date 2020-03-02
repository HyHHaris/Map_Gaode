package com.a01tech.map_gaode.fragments


import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.view.contains
import androidx.core.view.forEach
import androidx.core.view.forEachIndexed
import androidx.core.view.size
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.a01tech.map_gaode.MainActivity
import com.a01tech.map_gaode.MapApp
import com.a01tech.map_gaode.R
import com.a01tech.map_gaode.Zone
import com.a01tech.map_gaode.entity.UserInfo
import com.a01tech.map_gaode.interfaces.EditXunChaListener
import com.a01tech.map_gaode.retrofit.RetrofitUtil
import com.a01tech.map_gaode.utils.DialogUtil
import com.a01tech.map_gaode.utils.Glide4Engine
import com.a01tech.map_gaode.utils.JsonData
import com.a01tech.map_gaode.utils.ToastUtil
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.elvishew.xlog.XLog
import com.google.android.material.chip.Chip
import com.jakewharton.rxbinding3.widget.textChanges
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import com.qmuiteam.qmui.widget.popup.QMUIPopup
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.filter.Filter
import com.zhihu.matisse.internal.entity.CaptureStrategy
import com.zhihu.matisse.internal.entity.IncapableCause
import com.zhihu.matisse.internal.entity.Item
import com.zhihu.matisse.internal.utils.PhotoMetadataUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_shangbao.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.util.*


class ShangbaoFragment : Fragment() {

    private val compDis = CompositeDisposable()

    override fun onDestroy() {
        super.onDestroy()
        compDis.clear()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_shangbao, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance() = ShangbaoFragment()
    }


    private fun selectPic() {
        Matisse.from(this)
            .choose(MimeType.ofImage())
//            .addFilter(TileFilter())
//            .addFilter(GifSizeFilter(300, 300, 5 * Filter.K * Filter.K))
            .capture(false)
            .captureStrategy(CaptureStrategy(true, "com.a01tech.map_gaode.fileprovider"))
            .countable(false)
            .maxSelectable(9)
            .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
            .thumbnailScale(0.85f)
            .imageEngine(Glide4Engine())
            .forResult(1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            /* val d = dataChubei.getStringArrayListExtra(EasyPhotos.RESULT_PATHS)
             if (d.isNotEmpty()) {
                 val path = d.first()
                 addImage(path)
             }*/

            val result = Matisse.obtainPathResult(data)
            if (result.isNotEmpty()) {
                result.forEach {
                    addImage(it)
                }
//                val path = result.first()
//                addImage(path)
            }
        }
    }

    lateinit var adapter: UploadAdapter

    private fun getRenyuan(): String {
        var s = ""
        chipGroup.forEachIndexed { index, view ->
            if (index != chipGroup.size - 1) {
                if (index == 0) {
                    s += (view as Chip).text
                } else {
                    s += "," + (view as Chip).text
                }
            }
        }
        return s
    }

    lateinit var popup: QMUIPopup
    lateinit var popupRcv: RecyclerView
    lateinit var popupEditText: EditText
    lateinit var popupBtnAdd: QMUIRoundButton
    lateinit var popupAdapter: NamesAdapter


    @SuppressLint("CheckResult")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = UploadAdapter()
        val footerView = layoutInflater.inflate(R.layout.item_upload_img_add, rcvUpload.parent as ViewGroup, false)
        footerView.setOnClickListener { selectPic() }
        adapter.isFooterViewAsFlow = true
        adapter.addFooterView(footerView)
        rcvUpload.adapter = adapter

        rcvUpload.layoutManager = GridLayoutManager(context, 3)

        edtXunChaJiLu.textChanges().subscribe {
            textCount.text = "${it.length}/300"
        }

        adapter.setOnItemChildClickListener { _, view, position ->
            adapter.remove(position)
        }


        popup = QMUIPopup(context)
        val view = layoutInflater.inflate(R.layout.popup_select_name, null)
        popup.setContentView(view)
        popupEditText = view.findViewById(R.id.editText)
        popupBtnAdd = view.findViewById(R.id.btnAdd)
        popupRcv = view.findViewById(R.id.rcv)
        popupAdapter = NamesAdapter()
        popupRcv.layoutManager = LinearLayoutManager(context)
        popupRcv.adapter = popupAdapter

        popupAdapter.setOnItemClickListener { _, view, position ->
            addName(popupAdapter.data[position])
        }
        popup.setAnimStyle(QMUIPopup.ANIM_GROW_FROM_CENTER)
        popup.setPreferredDirection(QMUIPopup.DIRECTION_TOP)

        popupBtnAdd.setOnClickListener {
            val s = popupEditText.text.toString().trim()
            if (s.isNotEmpty())
                addName(s)
        }


        btnSub.setOnClickListener {
            if (fromType == 0) {
                val content = getRenyuan()
                val situation = edtXunChaJiLu.text.trim().toString()

                /* if (situation.isEmpty()) {
                     ToastUtil.showToast(context, "请填写巡查记录")
                     return@setOnClickListener
                 }*/
                if (content.isEmpty()) {
                    ToastUtil.showToast(context, "请填写巡查人员")
                    return@setOnClickListener
                }
                /*               if (adapter.data.isEmpty()) {
                                   ToastUtil.showToast(context, "请上传图片")
                                   return@setOnClickListener
                               }
               */
                if (adapter.data.any { it.uploadStatus == 0 }) {
                    ToastUtil.showToast(context, "图片上传中")
                    return@setOnClickListener
                }

                val dialog = DialogUtil.genUploadingDialog(context!!)
                dialog.show()

                var ids = ""
                val d = adapter.data.filter {
                    it.uploadStatus == 1
                }
                d.forEachIndexed { index, uploadImgBean ->
                    ids += if (index == 0) {
                        uploadImgBean.id
                    } else {
                        ",${uploadImgBean.id}"
                    }
                }
                RetrofitUtil.getService()
                    .tijiaoXuncha(
                        MapApp.user.token, /*zone.id.toString()*/
                        if (zone.cbbh == null) "" else zone.cbbh!!,
                        guiji,
                        content,
                        situation,
                        ids
                    ).subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map { JsonData(it) }
                    .subscribe({
                        ToastUtil.showToast(context, it.msg)
                        if (it.code == 1) {
                            listener?.back(fromType)
                        }
                        dialog.dismiss()
                    }, {
                        it.printStackTrace()
                        dialog.dismiss()
                        ToastUtil.showToast(context, "ERROR")
                    })
            } else {
                val content = getRenyuan()
                val situation = edtXunChaJiLu.text.trim().toString()

                /*  if (situation.isEmpty()) {
                      ToastUtil.showToast(context, "请填写巡查记录")
                      return@setOnClickListener
                  }*/
                if (content.isEmpty()) {
                    ToastUtil.showToast(context, "请填写巡查人员")
                    return@setOnClickListener
                }
                /* if (adapter.data.isEmpty()) {
                     ToastUtil.showToast(context, "请上传图片")
                     return@setOnClickListener
                 }
 */
                if (adapter.data.any { it.uploadStatus == 0 }) {
                    ToastUtil.showToast(context, "图片上传中")
                    return@setOnClickListener
                }

                val dialog = DialogUtil.genUploadingDialog(context!!)
                dialog.show()

                var ids = ""
                val d = adapter.data.filter {
                    it.uploadStatus == 1 || it.uploadStatus == 3
                }
                d.forEachIndexed { index, uploadImgBean ->
                    ids += if (index == 0) {
                        uploadImgBean.id
                    } else {
                        ",${uploadImgBean.id}"
                    }
                }

                RetrofitUtil.getService()
                    .xiugaiXuncha(
                        MapApp.user.token, xunChaBean.id, content, situation, ids
                    ).subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map { JsonData(it) }
                    .subscribe({
                        ToastUtil.showToast(context, it.msg)
                        if (it.code == 1) {
                            listener?.back(fromType)
                        }
                        dialog.dismiss()
                    }, {
                        it.printStackTrace()
                        dialog.dismiss()
                        ToastUtil.showToast(context, "ERROR")
                    })
            }
        }


        btnReturn.setOnClickListener {
            listener?.back(fromType)
        }


        btnCancel.setOnClickListener {
            QMUIDialog.MessageDialogBuilder(context)
                .setTitle("提示")
                .setMessage("是否要放弃？")
                .addAction("取消") { a, b ->
                    a.dismiss()
                }.addAction("放弃") { a, b ->
                    listener?.back(fromType)
                    a.dismiss()
                }.create()
                .show()
        }

        loadNames()
    }

    @SuppressLint("CheckResult")
    private fun loadNames() {
        RetrofitUtil.getService()
            .getAllPatroler(MapApp.user.token)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .map { JsonData(it) }
            .subscribe({ it ->
                if (it.code == 1) {
                    val userInfos = it.getDataList("list", UserInfo::class.java)
                    popupAdapter.replaceData(
                        userInfos.map { user ->
                            user.nickName
                        }
                    )

                } else ToastUtil.showToast(context, it.msg)
            }, { it.printStackTrace() })
    }

    private fun addImage(path: String) {

        if (adapter.data.any { it.path == path }) {
            ToastUtil.showToast(context, "此图片已添加")
            return
        }

        val file = File(path)
        val uploadPart = MultipartBody.Part.createFormData(
            "upload", file.name,
            RequestBody.create(MediaType.parse("multipart/from-d"), file)
        )

        adapter.addData(UploadImgBean(path, 0))
        RetrofitUtil.getService()
            .uploadImg(uploadPart)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .map { JsonData(it) }
            .subscribe({
                val id = it.obj.get("patrolImg").asJsonObject.get("id").asString
                adapter.data.find { it.path == path }?.let {
                    it.uploadStatus = 1
                    it.id = id!!
                    adapter.notifyDataSetChanged()
                }
            }, {
                it.printStackTrace()
                adapter.data.find { it.path == path }?.let {
                    it.uploadStatus = 2
                    adapter.notifyDataSetChanged()
                }
            })
            .apply {
                compDis.add(this)
            }

    }

    lateinit var zone: Zone
    var guiji = ""
    var fromType = 0

    fun setData(tdId: Long, guiji: String) {
        fromType = 0
        this.zone = (activity as MainActivity).dataChubei.find {
            it.id == tdId
        }!!
        textTitle.text = "地图数据"
        this.guiji = guiji
        textGuijiName.text = guiji
        edtXunChaJiLu.setText("")
        setChipData(MapApp.user.nickName)
        textDikuaiName.text = zone.projectName + " " + zone.mapNumber

        adapter.data.clear()
        adapter.notifyDataSetChanged()
    }


    lateinit var xunChaBean: XunChaBean
    fun setData(fromType: Int, msg: String) {
        val d = MapApp.gson.fromJson(msg, XunChaBean::class.java)
        xunChaBean = d
        /* var piIds: String,
         var tdzl: String,
         var trajectory: String,
         var content: String,
         var situation: String,
         var createTime: Long*/

        textDikuaiName.text = d.name
        edtXunChaJiLu.setText(d.situation)
        setChipData(d.content)
        adapter.data.clear()
        adapter.notifyDataSetChanged()
        guiji = d.trajectory
        textGuijiName.text = d.trajectory

        adapter.replaceData(d.piIds.split(",").filter {
            it.isNotEmpty()
        }.map {
            UploadImgBean("", 3, it)
        })


        this.fromType = fromType
        textTitle.text = when (fromType) {
            1 -> "巡查管理"
            2 -> "巡查历史"
            else -> ""
        }
    }

    private fun setChipData(ss: String) {
        chipGroup.removeAllViews()
        val list = ss.split(",").filter {
            it.isNotEmpty()
        }

        for (s in list) {
            if (s.isNotEmpty()) {
                chipGroup.addView(
                    Chip(context).apply {
                        if (s != MapApp.user.nickName) {
                            isCloseIconVisible = true
                            setOnCloseIconClickListener {
                                if (chipGroup.contains(it)) {
                                    chipGroup.removeView(it)
                                }
                            }
                            text = s
                        } else {
                            text = s
                        }
//                        setChipBackgroundColorResource(R.color.qmui_config_color_white)
                        setTextColor(resources.getColor(R.color.normal_blue))
                        setChipStrokeColorResource(R.color.normal_blue)
                    }
                )
            }
        }

        chipGroup.addView(
            Chip(context).apply {
                setChipIconResource(R.drawable.ic_add_black_24dp)
                text = "添加"
                setTextColor(Color.parseColor("#99000000"))
                setOnClickListener {
                    showAddName(it)
                }
            }
        )

    }

    private fun showAddName(view: View) {
        popup.show(view)
    }

    private fun addName(s: String) {
        var contained = false

        chipGroup.forEach {
            if (it is Chip) {
                if (!contained && it.text.toString() == s) {
                    contained = true
                }
            }
        }

        if (!contained) {
            chipGroup.addView(
                Chip(context).apply {
                    if (s != MapApp.user.nickName) {
                        isCloseIconVisible = true
                        setOnCloseIconClickListener {
                            if (chipGroup.contains(it)) {
                                chipGroup.removeView(it)
                            }
                        }
                        text = s
                    } else {
                        text = s
                    }
                    setTextColor(resources.getColor(R.color.normal_blue))
                }, chipGroup.size - 1
            )
        }
    }

    private var listener: EditXunChaListener? = null

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        if (activity is EditXunChaListener) {
            listener = activity
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
}

class UploadImgBean(
    var path: String = "",
    var uploadStatus: Int = 0,
    var id: String = ""
)

class UploadAdapter : BaseQuickAdapter<UploadImgBean, BaseViewHolder>(R.layout.item_upload_img) {
    @SuppressLint("CheckResult")
    override fun convert(helper: BaseViewHolder, item: UploadImgBean) {
        if (item.uploadStatus == 3) {
            RetrofitUtil.getService()
                .photoDetail(MapApp.user.token, item.id)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .map { JsonData(it) }
                .subscribe(
                    {
                        if (it.code == 1) {
                            val path = it.obj.get("patrolImg").asJsonObject.get("img").asString
                            Glide.with(mContext).load(path).into(helper.getView(R.id.img))
                        }
                    }, { it.printStackTrace() }
                )
        } else {
            Glide.with(mContext).load(item.path).into(helper.getView(R.id.img))
        }

        helper.setText(
            R.id.textStatus, when (item.uploadStatus) {
                0 -> "上传中"
                1 -> "已上传"
                2 -> "上传失败"
                else -> ""
            }
        ).addOnClickListener(R.id.btnCancel)

    }
}


class NamesAdapter : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_name) {
    override fun convert(helper: BaseViewHolder, item: String) {
        helper.setText(R.id.text, item)
    }
}

internal class GifSizeFilter(private val mMinWidth: Int, private val mMinHeight: Int, private val mMaxSize: Int) :
    Filter() {

    public override fun constraintTypes(): Set<MimeType> {
        return object : HashSet<MimeType>() {
            init {
                add(MimeType.JPEG)
            }
        }
    }

    override fun filter(context: Context, item: Item): IncapableCause? {
        XLog.e("filter$item")
        if (!needFiltering(context, item))
            return null

        val size = PhotoMetadataUtils.getBitmapBound(context.contentResolver, item.contentUri)
        if (size.x < mMinWidth || size.y < mMinHeight || item.size > mMaxSize) {
            return IncapableCause(IncapableCause.DIALOG, "...")
        }
        return null
    }

}