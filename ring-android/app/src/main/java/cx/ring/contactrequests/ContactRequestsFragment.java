/*
 *  Copyright (C) 2004-2019 Savoir-faire Linux Inc.
 *
 *  Author: Aline Bonnet <aline.bonnet@savoirfairelinux.com>
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cx.ring.contactrequests;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import cx.ring.R;
import cx.ring.adapters.SmartListAdapter;
import cx.ring.application.JamiApplication;
import cx.ring.client.ConversationActivity;
import cx.ring.client.HomeActivity;
import cx.ring.databinding.FragPendingContactRequestsBinding;
import cx.ring.mvp.BaseSupportFragment;
import cx.ring.smartlist.SmartListViewModel;
import cx.ring.utils.ConversationPath;
import cx.ring.utils.DeviceUtils;
import cx.ring.viewholders.SmartListViewHolder;

public class ContactRequestsFragment extends BaseSupportFragment<ContactRequestsPresenter> implements ContactRequestsView,
        SmartListViewHolder.SmartListListeners {

    private  static final String TAG = ContactRequestsFragment.class.getSimpleName();
    public static final String ACCOUNT_ID = TAG + "accountID";

    private static final int SCROLL_DIRECTION_UP = -1;

    private SmartListAdapter mAdapter;
    private FragPendingContactRequestsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragPendingContactRequestsBinding.inflate(inflater, container, false);
        ((JamiApplication) requireActivity().getApplication()).getInjectionComponent().inject(this);
        setHasOptionsMenu(true);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mAdapter = null;
        binding = null;
    }

    public void presentForAccount(@NonNull String accountId) {
        presenter.updateAccount(accountId);
        Bundle arguments = getArguments();
        if (arguments != null)
            arguments.putString(ACCOUNT_ID, accountId);
    }

    @Override
    public void onStart() {
        super.onStart();
        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(ACCOUNT_ID)) {
            presenter.updateAccount(getArguments().getString(ACCOUNT_ID));
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.clear();
    }

    @Override
    public void updateView(final List<SmartListViewModel> list) {
        if (binding == null) {
            return;
        }

        if (!list.isEmpty()) {
            binding.paneRingID.setVisibility(/*viewModel.hasPane() ? View.VISIBLE :*/ View.GONE);
        }

        binding.placeholder.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);

        if (binding.requestsList.getAdapter() != null) {
            mAdapter.update(list);
        } else {
            mAdapter = new SmartListAdapter(list, ContactRequestsFragment.this);
            binding.requestsList.setAdapter(mAdapter);
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
            binding.requestsList.setLayoutManager(mLayoutManager);
        }

        binding.requestsList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                ((HomeActivity) requireActivity()).setToolbarElevation(recyclerView.canScrollVertically(SCROLL_DIRECTION_UP));
            }
        });

        updateBadge();
    }

    @Override
    public void updateItem(SmartListViewModel item) {
        if (mAdapter != null) {
            mAdapter.update(item);
        }
    }

    @Override
    public void goToConversation(String accountId, String contactId) {
        Context context = requireContext();
        if (DeviceUtils.isTablet(context)) {
            Activity activity = getActivity();
            if (activity instanceof HomeActivity) {
                ((HomeActivity) activity).startConversationTablet(ConversationPath.toBundle(accountId, contactId));
            }
        } else {
            startActivity(new Intent(Intent.ACTION_VIEW, ConversationPath.toUri(accountId, contactId), context, ConversationActivity.class));
        }
    }

    @Override
    public void onItemClick(SmartListViewModel viewModel) {
        presenter.contactRequestClicked(viewModel.getAccountId(), viewModel.getContact());
    }

    @Override
    public void onItemLongClick(SmartListViewModel smartListViewModel) {

    }

    private void updateBadge() {
        ((HomeActivity) requireActivity()).setBadge(R.id.navigation_requests, mAdapter.getItemCount());
    }
}
