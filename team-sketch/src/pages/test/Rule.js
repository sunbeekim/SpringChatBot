import React, { useState, useEffect } from 'react';

import { fetchRulesList, addRule, updateRule, deleteRule, applyRule, getAppliedRules, unapplyRule } from '../../api/testAPI';
import SimpleRule from './SimpleRule';
import ConditionRule from './ConditionRule';
import AppliedRules from './AppliedRules';


const Rule = () => {
    const { state } = { state: { user: { roleId: 1, username: 'subadmin' } } };

    const [rules, setRules] = useState([]);
    const [appliedRules, setAppliedRules] = useState([]);
    const [newRule, setNewRule] = useState({
        type: 'simple',  // 'simple' 또는 'conditional'
        triggerWords: '',
        response: '',
        conditions: [] // 조건부 응답을 위한 배열
    });
    const [openDialog, setOpenDialog] = useState(false);
    const [selectedRule, setSelectedRule] = useState(null);
    const [showExample, setShowExample] = useState(false);

    // 조건 추가를 위한 상태
    const [showConditionForm, setShowConditionForm] = useState(false);
    const [newCondition, setNewCondition] = useState({
        condition: '',
        response: ''
    });

    const [isSelectionMode, setIsSelectionMode] = useState(false);
    const [selectedRules, setSelectedRules] = useState([]);

    const exampleRules = [
        {
            type: 'simple',
            triggerWords: "안녕, 하이, 반가워, hello, hi, 헬로",
            response: "안녕하세요! 저는 다양한 주제에 대해 대화할 수 있는 AI 어시스턴트입니다. 무엇을 도와드릴까요?"
        },
        {
            type: 'simple',
            triggerWords: "음식, 맛집, 식당, 레스토랑",
            response: "맛집을 찾으시나요? 어떤 종류의 음식을 좋아하시나요? 한식, 중식, 일식, 양식 등 선호하시는 음식 종류를 말씀해주시면 추천해드리겠습니다."
        },
        {
            type: 'simple',
            triggerWords: "취미, 관심사, 여가, 시간",
            response: "취미 생활은 삶의 질을 높여주죠. 요리, 그림, 음악, 원예 등 다양한 취미 활동을 시작해보는 건 어떨까요? 관심 있는 분야가 있으시다면 말씀해주세요."
        },
        {
            type: 'conditional',
            triggerWords: "영화",
            conditions: [
                {
                    condition: "추천",
                    response: "최근 인기 있는 영화로는 '외계+인', '콘크리트 유토피아', '밀수' 등이 있습니다."
                },
                {
                    condition: "줄거리",
                    response: "어떤 영화의 줄거리가 궁금하신가요? 영화 제목을 말씀해주시면 자세히 알려드리겠습니다."
                }
            ],
            response: "영화에 관심이 있으시군요! 추천이나 줄거리 등 구체적으로 말씀해주시면 더 자세히 답변드릴 수 있습니다."
        },
        {
            type: 'conditional',
            triggerWords: "여행",
            conditions: [
                {
                    condition: "국내",
                    response: "국내 여행지로는 제주도, 부산, 강원도가 인기가 있습니다. 계절과 목적에 따라 더 자세히 추천해드릴 수 있어요."
                },
                {
                    condition: "해외",
                    response: "해외 여행지로는 일본, 동남아, 유럽 등이 인기가 있습니다. 어떤 지역에 관심이 있으신가요?"
                }
            ],
            response: "여행 계획을 세우고 계시나요? 국내/해외 선호하시는 지역을 말씀해주시면 더 자세한 정보를 알려드릴 수 있습니다."
        },
        {
            type: 'conditional',
            triggerWords: "운동",
            conditions: [
                {
                    condition: "다이어트",
                    response: "체중 감량을 위해서는 유산소 운동이 효과적입니다. 걷기, 조깅, 수영 등이 좋습니다."
                },
                {
                    condition: "근력",
                    response: "근력 운동은 웨이트 트레이닝이 가장 효과적입니다. 처음이시라면 전문가와 함께 시작하시는 것을 추천드립니다."
                }
            ],
            response: "운동은 목적에 따라 적절한 방법이 다릅니다. 다이어트가 목적이신지, 근력 강화가 목적이신지 알려주시면 더 자세히 안내해드리겠습니다."
        }
    ];

    useEffect(() => {
        fetchRules();
        fetchAppliedRules();
    }, []);

    const fetchRules = async () => {
        try {
            const data = await fetchRulesList(state.user.roleId, state.user.username);
            setRules(data);
            console.log(data);
        } catch (error) {
            console.error('규칙 불러오기 실패:', error);
        }
    };

    const fetchAppliedRules = async () => {
        try {
            const data = await getAppliedRules();
            setAppliedRules(data);
        } catch (error) {
            console.error('적용된 규칙 불러오기 실패:', error);
        }
    };

    const handleAddRule = async () => {
        try {
            const triggerWordsArray = newRule.triggerWords
                .split(',')
                .map(word => word.trim())
                .filter(word => word.length > 0);

            const ruleData = {
                type: newRule.type,
                triggerWords: triggerWordsArray,
                response: newRule.response
            };

            if (newRule.type === 'conditional') {
                ruleData.conditions = newRule.conditions;
            }

            await addRule(ruleData, state.user.roleId, state.user.username);
            
            setNewRule({
                type: 'simple',
                triggerWords: '',
                response: '',
                conditions: []
            });
            setShowConditionForm(false);
            fetchRules();
        } catch (error) {
            console.error('규칙 추가 실패:', error);
        }
    };

    const handleEditRule = (rule) => {
        setSelectedRule({
            ...rule,
            triggerWords: Array.isArray(rule.triggerWords) 
                ? rule.triggerWords.join(', ')
                : rule.triggerWords,
            conditions: rule.conditions?.map(condition => ({
                ...condition,
                conditionText: condition.conditionText || condition.condition
            })) || []
        });
        setOpenDialog(true);
    };

    const handleUpdateRule = async () => {
        try {
            const triggerWordsArray = selectedRule.triggerWords
                .split(',')
                .map(word => word.trim())
                .filter(word => word.length > 0);

            await updateRule(
                selectedRule.id, 
                { ...selectedRule, triggerWords: triggerWordsArray },
                state.user.roleId,
                state.user.username
            );
            setOpenDialog(false);
            fetchRules();
        } catch (error) {
            console.error('규칙 수정 실패:', error);
        }
    };

    const handleDeleteRule = async (id) => {
        try {
            await deleteRule(id, state.user.roleId, state.user.username);
            fetchRules();
        } catch (error) {
            console.error('규칙 삭제 실패:', error);
        }
    };

    const handleAddCondition = () => {
        setNewRule({
            ...newRule,
            conditions: [...newRule.conditions, newCondition]
        });
        setNewCondition({ condition: '', response: '' });
        setShowConditionForm(false);
    };

    const handleRemoveCondition = (index) => {
        const updatedConditions = newRule.conditions.filter((_, i) => i !== index);
        setNewRule({
            ...newRule,
            conditions: updatedConditions
        });
    };

    const toggleSelectionMode = () => {
        setIsSelectionMode(!isSelectionMode);
        setSelectedRules([]);
    };

    const handleSelectRule = (ruleId) => {
        setSelectedRules(prev => 
            prev.includes(ruleId) 
                ? prev.filter(id => id !== ruleId)
                : [...prev, ruleId]
        );
    };

    const handleSelectAllRules = (type) => {
        const typeRules = rules.filter(rule => rule.type === type);
        const typeRuleIds = typeRules.map(rule => rule.id);
        
        if (typeRules.every(rule => selectedRules.includes(rule.id))) {
            setSelectedRules(prev => prev.filter(id => !typeRuleIds.includes(id)));
        } else {
            setSelectedRules(prev => {
                const filteredPrev = prev.filter(id => !typeRuleIds.includes(id));
                return [...filteredPrev, ...typeRuleIds];
            });
        }
    };

    const handleApplySelectedRules = async () => {
        try {
            for (const ruleId of selectedRules) {
                await applyRule(ruleId, state.user.username);
            }
            await fetchAppliedRules();
            setIsSelectionMode(false);
            setSelectedRules([]);
            alert('선택한 규칙들이 성공적으로 적용되었습니다.');
        } catch (error) {
            console.error('규칙 적용 실패:', error);
            alert('규칙 적용에 실패했습니다.');
        }
    };

    const handleUnapplyRules = async (ruleIds) => {
        try {
            for (const ruleId of ruleIds) {
                await unapplyRule(ruleId, state.user.username);
            }
            await fetchRules();
            await fetchAppliedRules();
        } catch (error) {
            console.error('규칙 적용 해제 실패:', error);
        }
    };

    const getUnappliedSelectedCount = () => {
        return selectedRules.filter(id => {
            const rule = rules.find(r => r.id === id);
            return rule && !rule.applied;
        }).length;
    };

    return (
        <div className="container py-4">
            <div className="row mb-4">
                <div className="col">
                    <h1 className="mb-3">챗봇 응답 규칙 관리</h1>
                    <button 
                        onClick={() => setShowExample(!showExample)} 
                        className="btn btn-outline-primary"
                    >
                        {showExample ? '예시 숨기기' : '예시 보기'}
                    </button>
                </div>
            </div>

            {showExample && (
                <div className="card mb-4">
                    <div className="card-body">
                        <h2 className="card-title mb-3">단순 응답 규칙 예시</h2>
                        <div className="table-responsive">
                            <table className="table table-striped table-hover">
                                <thead className="table-primary">
                                    <tr>
                                        <th>트리거 단어</th>
                                        <th>응답 내용</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {exampleRules
                                        .filter(rule => rule.type === 'simple')
                                        .map((rule, index) => (
                                            <tr key={index}>
                                                <td>{rule.triggerWords}</td>
                                                <td>{rule.response}</td>
                                            </tr>
                                        ))
                                    }
                                </tbody>
                            </table>
                        </div>

                        <h2 className="card-title mt-4 mb-3">조건부 응답 규칙 예시</h2>
                        <div className="table-responsive">
                            <table className="table table-striped table-hover">
                                <thead className="table-primary">
                                    <tr>
                                        <th>트리거 단어</th>
                                        <th>조건</th>
                                        <th>조건별 응답</th>
                                        <th>기본 응답</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {exampleRules
                                        .filter(rule => rule.type === 'conditional')
                                        .map((rule, index) => (
                                            <tr key={index}>
                                                <td>{rule.triggerWords}</td>
                                                <td>{rule.conditions.map(c => c.condition).join(', ')}</td>
                                                <td>
                                                    {rule.conditions.map((c, i) => (
                                                        <div key={i} className="mb-1">
                                                            <small className="text-muted">IF</small> "{c.condition}": 
                                                            <br />
                                                            <span className="ms-2">"{c.response}"</span>
                                                        </div>
                                                    ))}
                                                </td>
                                                <td>{rule.response}</td>
                                            </tr>
                                        ))
                                    }
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            )}

            <div className="card mb-4">
                <div className="card-body">
                    <h3 className="card-title mb-3">새 규칙 추가</h3>
                    <div className="mb-3">
                        <select
                            value={newRule.type}
                            onChange={(e) => setNewRule({...newRule, type: e.target.value})}
                            className="form-select"
                        >
                            <option value="simple">단순 응답</option>
                            <option value="conditional">조건부 응답</option>
                        </select>
                    </div>

                    <div className="mb-3">
                        <input
                            type="text"
                            placeholder="트리거 단어들을 쉼표로 구분하여 입력하세요"
                            value={newRule.triggerWords}
                            onChange={(e) => setNewRule({...newRule, triggerWords: e.target.value})}
                            className="form-control"
                        />
                    </div>

                    {newRule.type === 'conditional' ? (
                        <div className="mb-3">
                            <div className="d-flex justify-content-between align-items-center mb-2">
                                <h4 className="mb-0">조건들</h4>
                                <button 
                                    onClick={() => setShowConditionForm(true)}
                                    className="btn btn-outline-primary btn-sm"
                                >
                                    조건 추가
                                </button>
                            </div>

                            {showConditionForm && (
                                <div className="card mb-3">
                                    <div className="card-body">
                                        <div className="mb-2">
                                            <input
                                                type="text"
                                                placeholder="조건을 입력하세요"
                                                value={newCondition.condition}
                                                onChange={(e) => setNewCondition({
                                                    ...newCondition,
                                                    condition: e.target.value
                                                })}
                                                className="form-control mb-2"
                                            />
                                            <textarea
                                                placeholder="조건에 해당하는 응답을 입력하세요"
                                                value={newCondition.response}
                                                onChange={(e) => setNewCondition({
                                                    ...newCondition,
                                                    response: e.target.value
                                                })}
                                                className="form-control mb-2"
                                                rows="2"
                                            />
                                            <div className="d-flex justify-content-end gap-2">
                                                <button 
                                                    onClick={() => setShowConditionForm(false)}
                                                    className="btn btn-outline-secondary"
                                                >
                                                    취소
                                                </button>
                                                <button 
                                                    onClick={handleAddCondition}
                                                    className="btn btn-primary"
                                                >
                                                    추가
                                                </button>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            )}

                            {newRule.conditions.map((condition, index) => (
                                <div key={index} className="card mb-2">
                                    <div className="card-body">
                                        <div className="d-flex justify-content-between align-items-start">
                                            <div>
                                                <div className="fw-bold">조건: {condition.condition}</div>
                                                <div>응답: {condition.response}</div>
                                            </div>
                                            <button 
                                                onClick={() => handleRemoveCondition(index)}
                                                className="btn btn-outline-danger btn-sm"
                                            >
                                                삭제
                                            </button>
                                        </div>
                                    </div>
                                </div>
                            ))}

                            <div className="mb-3">
                                <h4>기본 응답</h4>
                                <textarea
                                    placeholder="조건에 해당하지 않을 때의 기본 응답을 입력하세요"
                                    value={newRule.response}
                                    onChange={(e) => setNewRule({...newRule, response: e.target.value})}
                                    className="form-control"
                                    rows="3"
                                />
                            </div>
                        </div>
                    ) : (
                        <div className="mb-3">
                            <textarea
                                placeholder="응답 내용을 입력하세요"
                                value={newRule.response}
                                onChange={(e) => setNewRule({...newRule, response: e.target.value})}
                                className="form-control"
                                rows="3"
                            />
                        </div>
                    )}

                    <button 
                        onClick={handleAddRule} 
                        className="btn btn-primary"
                    >
                        규칙 추가
                    </button>
                </div>
            </div>

            <div className="d-flex justify-content-between align-items-center mb-3">
                <button 
                    className="btn btn-outline-primary"
                    onClick={toggleSelectionMode}
                >
                    {isSelectionMode ? '규칙 적용' : '규칙 선택'}
                </button>
                {isSelectionMode && selectedRules.length > 0 && (
                    <button 
                        className="btn btn-success"
                        onClick={handleApplySelectedRules}
                    >
                        선택한 규칙 적용하기 ({getUnappliedSelectedCount()}개)
                    </button>
                )}
            </div>

            <SimpleRule
                rules={rules}
                isSelectionMode={isSelectionMode}
                selectedRules={selectedRules}
                handleSelectRule={handleSelectRule}
                handleSelectAllRules={handleSelectAllRules}
                handleEditRule={handleEditRule}
                handleDeleteRule={handleDeleteRule}
            />

            <ConditionRule
                rules={rules}
                isSelectionMode={isSelectionMode}
                selectedRules={selectedRules}
                handleSelectRule={handleSelectRule}
                handleSelectAllRules={handleSelectAllRules}
                handleEditRule={handleEditRule}
                handleDeleteRule={handleDeleteRule}
            />

            <AppliedRules 
                rules={appliedRules} 
                onUnapplyRules={handleUnapplyRules}
            />

            {openDialog && selectedRule && (
                <div className="modal show d-block" style={{backgroundColor: 'rgba(0,0,0,0.5)'}}>
                    <div className="modal-dialog">
                        <div className="modal-content">
                            <div className="modal-header">
                                <h5 className="modal-title">규칙 수정</h5>
                                <button 
                                    type="button" 
                                    className="btn-close"
                                    onClick={() => setOpenDialog(false)}
                                ></button>
                            </div>
                            <div className="modal-body">
                                <div className="mb-3">
                                    <label className="form-label">트리거 단어</label>
                                    <input
                                        type="text"
                                        value={selectedRule.triggerWords}
                                        onChange={(e) => setSelectedRule({
                                            ...selectedRule,
                                            triggerWords: e.target.value
                                        })}
                                        className="form-control"
                                    />
                                </div>
                                <div className="mb-3">
                                    <label className="form-label">응답</label>
                                    <textarea
                                        value={selectedRule.response}
                                        onChange={(e) => setSelectedRule({
                                            ...selectedRule,
                                            response: e.target.value
                                        })}
                                        className="form-control"
                                        rows="3"
                                    />
                                </div>
                            </div>
                            <div className="modal-footer">
                                <button 
                                    type="button" 
                                    className="btn btn-secondary"
                                    onClick={() => setOpenDialog(false)}
                                >
                                    취소
                                </button>
                                <button 
                                    type="button" 
                                    className="btn btn-primary"
                                    onClick={handleUpdateRule}
                                >
                                    저장
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default Rule;
