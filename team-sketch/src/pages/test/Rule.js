import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './Rule.css';
import { fetchRulesList, addRule, updateRule, deleteRule, applyRule, getAppliedRules, unapplyRule } from '../../api/testAPI';
import SimpleRule from './SimpleRule';
import ConditionRule from './ConditionRule';
import AppliedRules from './AppliedRules';


const Rule = () => {
    const { state } = { state: { user: { roleId: 1, username: 'subadmin' } } };
    const isAuthenticated = true;

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

    return (
        <div className="rules-container">
            <h1>챗봇 응답 규칙 관리</h1>
            
            <button 
                onClick={() => setShowExample(!showExample)} 
                className="button example-button"
            >
                {showExample ? '예시 숨기기' : '예시 보기'}
            </button>

            {showExample && (
                <div className="example-rules">
                    <h2>단순 응답 규칙 예시</h2>
                    <div className="rules-table">
                        <table>
                            <thead>
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

                    <h2>조건부 응답 규칙 예시</h2>
                    <div className="rules-table">
                        <table>
                            <thead>
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
                                            <td>
                                                {rule.conditions.map(c => c.condition).join(', ')}
                                            </td>
                                            <td>
                                                {rule.conditions.map((c, i) => (
                                                    <div key={i}>
                                                        IF "{c.condition}": "{c.response}"
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
            )}
            
            <div className="rule-form">
                <select
                    value={newRule.type}
                    onChange={(e) => setNewRule({...newRule, type: e.target.value})}
                    className="select-field"
                >
                    <option value="simple">단순 응답</option>
                    <option value="conditional">조건부 응답</option>
                </select>

                <input
                    type="text"
                    placeholder="트리거 단어들 (쉼표로 구분)"
                    value={newRule.triggerWords}
                    onChange={(e) => setNewRule({...newRule, triggerWords: e.target.value})}
                    className="input-field"
                />

                {newRule.type === 'conditional' ? (
                    <div className="conditions-container">
                        <h3>조건부 응답 목록</h3>
                        {newRule.conditions.map((condition, index) => (
                            <div key={index} className="condition-item">
                                <p>IF "{condition.condition}" THEN "{condition.response}"</p>
                                <button 
                                    onClick={() => handleRemoveCondition(index)}
                                    className="button-small delete"
                                >
                                    삭제
                                </button>
                            </div>
                        ))}
                        
                        {showConditionForm ? (
                            <div className="condition-form">
                                <input
                                    type="text"
                                    placeholder="조건 단어"
                                    value={newCondition.condition}
                                    onChange={(e) => setNewCondition({
                                        ...newCondition,
                                        condition: e.target.value
                                    })}
                                    className="input-field"
                                />
                                <textarea
                                    placeholder="조건 응답"
                                    value={newCondition.response}
                                    onChange={(e) => setNewCondition({
                                        ...newCondition,
                                        response: e.target.value
                                    })}
                                    className="textarea-field"
                                    rows="2"
                                />
                                <div className="button-group">
                                    <button onClick={handleAddCondition} className="button-small">
                                        조건 추가
                                    </button>
                                    <button 
                                        onClick={() => setShowConditionForm(false)}
                                        className="button-small"
                                    >
                                        취소
                                    </button>
                                </div>
                            </div>
                        ) : (
                            <button 
                                onClick={() => setShowConditionForm(true)}
                                className="button-small"
                            >
                                새 조건 추가
                            </button>
                        )}

                        <div className="default-response">
                            <h3>기본 응답</h3>
                            <textarea
                                placeholder="조건에 해당하지 않을 때의 기본 응답을 입력하세요"
                                value={newRule.response}
                                onChange={(e) => setNewRule({...newRule, response: e.target.value})}
                                className="textarea-field"
                                rows="4"
                            />
                        </div>
                    </div>
                ) : (
                    <textarea
                        placeholder="응답 내용을 입력하세요"
                        value={newRule.response}
                        onChange={(e) => setNewRule({...newRule, response: e.target.value})}
                        className="textarea-field"
                        rows="4"
                    />
                )}

                <button onClick={handleAddRule} className="button">
                    규칙 추가
                </button>
            </div>

            <div className="rules-header">
                <button 
                    className="selection-button"
                    onClick={toggleSelectionMode}
                >
                    {isSelectionMode ? '규칙 적용' : '규칙 선택'}
                </button>
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

            {isSelectionMode && selectedRules.length > 0 && (
                <div className="apply-rules-section">
                    <button 
                        className="apply-rules-button"
                        onClick={handleApplySelectedRules}
                    >
                        선택한 규칙 적용하기 ({selectedRules.length}개)
                    </button>
                </div>
            )}

            {openDialog && (
                <div className="modal">
                    <div className="modal-content">
                        <h2>규칙 수정</h2>
                        <input
                            type="text"
                            placeholder="트리거 단어들 (쉼표로 구분)"
                            value={selectedRule.triggerWords}
                            onChange={(e) => setSelectedRule({
                                ...selectedRule, 
                                triggerWords: e.target.value
                            })}
                            className="input-field"
                        />
                        
                        {selectedRule.type === 'conditional' ? (
                            <div className="conditions-container">
                                <h3>조건부 응답 목록</h3>
                                {selectedRule.conditions?.map((condition, index) => (
                                    <div key={index} className="condition-item">
                                        <input
                                            type="text"
                                            placeholder="조건"
                                            value={condition.conditionText}
                                            onChange={(e) => {
                                                const updatedConditions = [...selectedRule.conditions];
                                                updatedConditions[index] = {
                                                    ...condition,
                                                    conditionText: e.target.value
                                                };
                                                setSelectedRule({
                                                    ...selectedRule,
                                                    conditions: updatedConditions
                                                });
                                            }}
                                            className="input-field"
                                        />
                                        <textarea
                                            placeholder="조건 응답"
                                            value={condition.response}
                                            onChange={(e) => {
                                                const updatedConditions = [...selectedRule.conditions];
                                                updatedConditions[index] = {
                                                    ...condition,
                                                    response: e.target.value
                                                };
                                                setSelectedRule({
                                                    ...selectedRule,
                                                    conditions: updatedConditions
                                                });
                                            }}
                                            className="textarea-field"
                                            rows="2"
                                        />
                                        <button 
                                            onClick={() => {
                                                const updatedConditions = selectedRule.conditions.filter((_, i) => i !== index);
                                                setSelectedRule({
                                                    ...selectedRule,
                                                    conditions: updatedConditions
                                                });
                                            }}
                                            className="button-small delete"
                                        >
                                            삭제
                                        </button>
                                    </div>
                                ))}
                                
                                <button 
                                    onClick={() => {
                                        setSelectedRule({
                                            ...selectedRule,
                                            conditions: [
                                                ...selectedRule.conditions,
                                                { conditionText: '', response: '' }
                                            ]
                                        });
                                    }}
                                    className="button-small"
                                >
                                    새 조건 추가
                                </button>

                                <div className="default-response">
                                    <h3>기본 응답</h3>
                                    <textarea
                                        placeholder="조건에 해당하지 않을 때의 기본 응답을 입력하세요"
                                        value={selectedRule.response}
                                        onChange={(e) => setSelectedRule({
                                            ...selectedRule, 
                                            response: e.target.value
                                        })}
                                        className="textarea-field"
                                        rows="4"
                                    />
                                </div>
                            </div>
                        ) : (
                            <textarea
                                value={selectedRule.response}
                                onChange={(e) => setSelectedRule({
                                    ...selectedRule, 
                                    response: e.target.value
                                })}
                                className="textarea-field"
                                rows="4"
                            />
                        )}
                        
                        <div className="modal-buttons">
                            <button 
                                onClick={() => setOpenDialog(false)}
                                className="button-small"
                            >
                                취소
                            </button>
                            <button 
                                onClick={handleUpdateRule}
                                className="button-small"
                            >
                                저장
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default Rule;
