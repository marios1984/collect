/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.openforis.collect.designer.form.CodeListFormObject;
import org.openforis.collect.designer.form.CodeListFormObject.Type;
import org.openforis.collect.designer.form.FormObject;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.designer.util.MessageUtil.ConfirmHandler;
import org.openforis.collect.designer.util.Resources;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeList.CodeScope;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.CodeListLevel;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.Binder;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.DependsOn;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Window;

/**
 * 
 * @author S. Ricci
 *
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class CodeListsVM extends SurveyObjectBaseVM<CodeList> {

	private static final String CODE_LISTS_UPDATED_GLOBAL_COMMAND = "codeListsUpdated";
	private static final String SURVEY_CODE_LIST_GENERATED_LEVEL_NAME_LABEL_KEY = "survey.code_list.generated_level_name";
	public static final String CLOSE_CODE_LIST_ITEM_POP_UP_COMMAND = "closeCodeListItemPopUp";

//	private DefaultTreeModel<CodeListItem> treeModel;
	
	private List<List<CodeListItem>> itemsPerLevel;
	private boolean newChildItem;
	private CodeListItem editedChildItem;
	private CodeListItem editedChildItemParentItem;
	private int editedChildItemLevel;
	
	private List<CodeListItem> selectedItemsPerLevel;
	private Window codeListItemPopUp;
	
	@Init(superclass=false)
	public void init(@ExecutionArgParam("selectedCodeList") CodeList selectedCodeList) {
		super.init();
		if ( selectedCodeList != null ) {
			selectionChanged(selectedCodeList);
		}
	}
	
	@Override
	protected List<CodeList> getItemsInternal() {
		CollectSurvey survey = getSurvey();
		List<CodeList> codeLists = survey.getCodeLists();
		codeLists = sortByName(codeLists);
		return codeLists;
	}

	protected List<CodeList> sortByName(List<CodeList> codeLists) {
		List<CodeList> result = new ArrayList<CodeList>(codeLists);
		Collections.sort(result, new Comparator<CodeList>() {
			@Override 
	        public int compare(CodeList c1, CodeList c2) {
	            return c1.getName().compareTo(c2.getName());
	        }
		});
		return result;
	}

	@Override
	protected void addNewItemToSurvey() {
		CollectSurvey survey = getSurvey();
		survey.addCodeList(editedItem);
		dispatchCodeListsUpdatedCommand();
	}

	@Override
	protected void deleteItemFromSurvey(CodeList item) {
		CollectSurvey survey = getSurvey();
		survey.removeCodeList(item);
		dispatchCodeListsUpdatedCommand();
	}
	
	@Override
	protected FormObject<CodeList> createFormObject() {
		return new CodeListFormObject();
	}

	protected void dispatchCodeListsUpdatedCommand() {
		BindUtils.postGlobalCommand(null, null, CODE_LISTS_UPDATED_GLOBAL_COMMAND, null);
	}
	
	@Command
	@Override
	protected void performNewItemCreation(Binder binder) {
		super.performNewItemCreation(binder);
		notifyChange("listLevels","itemsPerLevel","selectedItemsPerLevel");
	}
	
	@Override
	protected CodeList createItemInstance() {
		CodeList instance = survey.createCodeList();
		return instance;
	}
	
	@Override
	protected void performItemSelection(CodeList item) {
		super.performItemSelection(item);
		notifyChange("listLevels","itemsPerLevel","selectedItemsPerLevel");
	}
	
	@Override
	protected void moveSelectedItem(int indexTo) {
		survey.moveCodeList(selectedItem, indexTo);
	}
	
	@Override
	@Command
	public void deleteItem(@BindingParam("item") final CodeList item) {
		boolean inUse = isInUse(item);
		if ( inUse ) {
			MessageUtil.showWarning("survey.code_list.alert.cannot_delete_used_list");
		} else {
			super.deleteItem(item);
		}
	}

	protected boolean isInUse(CodeList item) {
		Schema schema = survey.getSchema();
		List<EntityDefinition> rootEntities = schema.getRootEntityDefinitions();
		Stack<EntityDefinition> stack = new Stack<EntityDefinition>();
		stack.addAll(rootEntities);
		while ( ! stack.isEmpty() ) {
			EntityDefinition entity = stack.pop();
			List<NodeDefinition> children = entity.getChildDefinitions();
			for (NodeDefinition defn : children) {
				if ( defn instanceof CodeAttributeDefinition ) {
					CodeList list = ((CodeAttributeDefinition) defn).getList();
					if ( list.getId() == item.getId() ) {
						return true;
					}
				} else if ( defn instanceof EntityDefinition ) {
					stack.push((EntityDefinition) defn);
				}
			};
		}
		return false;
	}
	
	@Command
	public void typeChanged(@BindingParam("type") String type) {
		Type typeEnum = CodeListFormObject.Type.valueOf(type);
		CodeScope scope;
		switch (typeEnum) {
		case HIERARCHICAL:
			scope = CodeScope.LOCAL;
			addLevel();
			break;
		default:
			editedItem.removeLevel(0);
			scope = CodeScope.SCHEME;
		}
		editedItem.setCodeScope(scope);
		CodeListFormObject fo = (CodeListFormObject) formObject;
		fo.setCodeScope(scope.name());
		fo.setType(type);
		notifyChange("formObject","listLevels");
	}
	
	@Command
	@NotifyChange({"listLevels"})
	public void addLevel() {
		List<CodeListLevel> levels = editedItem.getHierarchy();
		int levelPosition = levels.size() + 1;
		CodeListLevel level = new CodeListLevel();
		String generatedName = Labels.getLabel(SURVEY_CODE_LIST_GENERATED_LEVEL_NAME_LABEL_KEY, new Object[]{levelPosition});
		level.setName(generatedName);
		editedItem.addLevel(level);
	}

	@Command
	public void removeLevel() {
		final List<CodeListLevel> levels = editedItem.getHierarchy();
		if ( ! levels.isEmpty() ) {
			final int levelIndex = levels.size() - 1;
			if ( editedItem.hasItemsInLevel(levelIndex) ) {
				ConfirmHandler handler = new ConfirmHandler() {
					@Override
					public void onOk() {
						performRemoveLevel(levelIndex);
					}
				};
				MessageUtil.showConfirm(handler, "survey.code_list.alert.cannot_delete_non_empty_level");
			} else {
				performRemoveLevel(levelIndex);
			}
		}
	}

	protected void performRemoveLevel(int levelIndex) {
		editedItem.removeLevel(levelIndex);
		deselectItemsAfterLevel(levelIndex);
		initItemsPerLevel();
		notifyChange("listLevels","selectedItemsPerLevel","itemsPerLevel");
	}
	
	@Command
	@NotifyChange({"itemsPerLevel"})
	public void addItemInLevel(@BindingParam("levelIndex") int levelIndex) {
		if ( checkCanLeaveForm() ) {
			newChildItem = true;
			editedChildItemLevel = levelIndex;
			editedChildItem = editedItem.createItem();
			editedChildItem.setCodeList(editedItem);
			if ( editedChildItemLevel == 0 ) {
				editedChildItemParentItem = null;
			} else {
				editedChildItemParentItem = selectedItemsPerLevel.get(editedChildItemLevel - 1);
			}
			openChildItemEditPopUp();
		}
	}
	
	@Command
	@NotifyChange({"itemsPerLevel"})
	public void removeItem(@BindingParam("item") final CodeListItem item) {
		String messageKey;
		if ( item.hasChildItems() ) {
			messageKey = "survey.code_list.confirm.delete_non_empty_item";
		} else {
			messageKey = "survey.code_list.confirm.delete_item";
		}
		MessageUtil.showConfirm(new MessageUtil.ConfirmHandler() {
			@Override
			public void onOk() {
				performRemoveItem(item);
			}
		}, messageKey);
	}

	protected void performRemoveItem(CodeListItem item) {
		if ( isCodeListItemSelected(item) ) {
			int itemLevelIndex = item.getDepth() - 1;
			deselectItemsAfterLevel(itemLevelIndex);
		}
		CodeListItem parentItem = item.getParentItem();
		int id = item.getId();
		if ( parentItem == null ) {
			CodeList codeList = item.getCodeList();
			codeList.removeItem(id);
		} else {
			parentItem.removeChildItem(id);
		}
		initItemsPerLevel();
		notifyChange("itemsPerLevel","selectedItemsPerLevel");
	}
	
	@Command
	public void moveChildItem(@ContextParam(ContextType.TRIGGER_EVENT) DropEvent event) {
		Listitem dragged = (Listitem) event.getDragged();
		Listitem dropped = (Listitem) event.getTarget();
		CodeListItem draggedItem = dragged.getValue();
		CodeListItem droppedItem = dropped.getValue();
		int indexTo = getItemIndex(droppedItem);
		moveChildItem(draggedItem, indexTo);
	}
	
	@Override
	public void setEditedItem(CodeList editedItem) {
		super.setEditedItem(editedItem);
		selectedItemsPerLevel = new ArrayList<CodeListItem>();
		initItemsPerLevel();
	}
	
	@Command
	public void editCodeListItem(@BindingParam("item") CodeListItem item) {
		newChildItem = false;
		editedChildItem = item;
		editedChildItemParentItem = item.getParentItem();
		openChildItemEditPopUp();
	}
	
	protected String generateItemCode(CodeListItem item) {
		return "item_" + item.getId();
	}

	public void openChildItemEditPopUp() {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("item", editedChildItem);
		args.put("parentItem", editedChildItemParentItem);
		codeListItemPopUp = openPopUp(Resources.Component.CODE_LIST_ITEM_EDIT_POP_UP.getLocation(), true, args);
		Binder binder = (Binder) codeListItemPopUp.getAttribute("$BINDER$");
		validateForm(binder);
	}

	@Command
	@NotifyChange({"itemsPerLevel","selectedItemsPerLevel"})
	public void listItemSelected(@BindingParam("item") CodeListItem item, 
			@BindingParam("levelIndex") int levelIndex) {
		deselectItemsAfterLevel(levelIndex);
		selectedItemsPerLevel.add(item);
		initItemsPerLevel();
	}
	
	@DependsOn("listLevels")
	public boolean isMultipleLevelsPresent() {
		if ( editedItem != null ) {
			return editedItem.getHierarchy().size() > 1;
		} else {
			return false;
		}
	}
	
	protected void deselectItemsAfterLevel(int levelIndex) {
		int maxSelectedLevelIndex = selectedItemsPerLevel.size() - 1;
		for (int i = maxSelectedLevelIndex; i >= levelIndex; i --) {
			selectedItemsPerLevel.remove(i);
		}
	}
	
	protected void moveChildItem(CodeListItem item, int indexTo) {
		CodeListItem parentItem = item.getParentItem();
		int depth = item.getDepth();
		int levelIndex = depth - 1;
		List<CodeListItem> siblings;
		if ( parentItem == null ) {
			CodeList codeList = item.getCodeList();
			codeList.moveItem(item, indexTo);
			siblings = codeList.getItems();
		} else {
			parentItem.moveChildItem(item, indexTo);
			siblings = parentItem.getChildItems();
		}
		itemsPerLevel.set(levelIndex, siblings);
		notifyChange("itemsPerLevel");
	}

	protected int getItemIndex(CodeListItem item) {
		CodeListItem parentItem = item.getParentItem();
		int index;
		List<CodeListItem> siblings;
		if ( parentItem == null ) {
			CodeList codeList = item.getCodeList();
			siblings = codeList.getItems();
		} else {
			siblings = parentItem.getChildItems();
		}
		index = siblings.indexOf(item);
		return index;
	}
	
	@GlobalCommand
	public void closeCodeListItemPopUp(@BindingParam("undoChanges") boolean undoChanges) {
		closePopUp(codeListItemPopUp);
		codeListItemPopUp = null;
		if ( ! undoChanges ) {
			if ( newChildItem ) {
				addChildItemToCodeList();
			} else {
				BindUtils.postNotifyChange(null, null, editedChildItem, "*");
			}
		}
	}
	
	@Override
	protected void commitChanges() {
		super.commitChanges();
		dispatchCodeListsUpdatedCommand();
	}

	private void addChildItemToCodeList() {
		if ( editedChildItemParentItem == null ) {
			editedItem.addItem(editedChildItem);
		} else {
			editedChildItemParentItem.addChildItem(editedChildItem);
		}
		List<CodeListItem> itemsForCurrentLevel = itemsPerLevel.get(editedChildItemLevel);
		itemsForCurrentLevel.add(editedChildItem);
		deselectItemsAfterLevel(editedChildItemLevel);
		selectedItemsPerLevel.add(editedChildItem);
		initItemsPerLevel();
		notifyChange("itemsPerLevel","selectedItemsPerLevel");
	}

	protected void initItemsPerLevel() {
		itemsPerLevel = new ArrayList<List<CodeListItem>>();
		if ( editedItem != null ) {
			List<CodeListItem> items = new ArrayList<CodeListItem>(editedItem.getItems());
			itemsPerLevel.add(items);
			for (CodeListItem selectedItem : selectedItemsPerLevel) {
				List<CodeListItem> childItems = new ArrayList<CodeListItem>(selectedItem.getChildItems());
				itemsPerLevel.add(childItems);
			}
		}
	}
	
	public List<CodeListLevel> getListLevels() {
		List<CodeListLevel> levels = null;
		if ( editedItem != null ) {
			levels = editedItem.getHierarchy();
			if ( levels.isEmpty() ) {
				CodeListLevel fakeFirstLevel = new CodeListLevel();
				return Arrays.asList(fakeFirstLevel);
			}
		}
		return levels;
	}
	
	public List<CodeListItem> getSelectedItemsPerLevel() {
		return selectedItemsPerLevel;
	}
	
	@DependsOn("selectedItemsPerLevel")
	public int getLastSelectedLevelIndex() {
		return selectedItemsPerLevel.size() - 1;
	}
	
	public List<List<CodeListItem>> getItemsPerLevel() {
		return itemsPerLevel;
	}
	
	public boolean isCodeListItemSelected(CodeListItem item) {
		return selectedItemsPerLevel.contains(item);
	}
	
	
}
