package android.helper.ui.activity.jetpack

import android.helper.R
import android.helper.ui.activity.jetpack.lifecycle.LifecycleActivity
import android.view.View
import com.android.helper.base.BaseTitleActivity
import com.android.helper.interfaces.TagListener
import com.android.helper.utils.ClassUtil
import com.android.helper.utils.LogUtil

/**
 * JetPack的集合
 */
class JetPackMapActivity : BaseTitleActivity(), TagListener {

    override fun getTitleLayout(): Int {
        return R.layout.activity_jet_pack_map
    }

    override fun initView() {
        super.initView()
        setTitleContent("JetPack的集合")
    }

    override fun initListener() {
        super.initListener()
        setonClickListener(R.id.tv_lifecycle)
    }

    override fun onClick(v: View?) {
        super.onClick(v)
        when (v?.id) {
            R.id.tv_lifecycle -> {
                startActivity(LifecycleActivity::class.java)
            }
        }
    }

    override fun getTag(): String {
        return ClassUtil.getClassName(this)
    }
}