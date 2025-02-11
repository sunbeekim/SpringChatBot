import React, { useState } from 'react';

const AppliedRules = ({ rules, onUnapplyRules }) => {
    const [isSelectionMode, setIsSelectionMode] = useState(false);
    const [selectedRules, setSelectedRules] = useState([]);

    const handleSelectRule = (id) => {
        setSelectedRules(prev => 
            prev.includes(id) 
                ? prev.filter(ruleId => ruleId !== id)
                : [...prev, id]
        );
    };

    const handleSelectAllRules = (type) => {
        const typeRules = rules.filter(rule => rule.type === type);
        const allSelected = typeRules.every(rule => selectedRules.includes(rule.id));
        
        if (allSelected) {
            setSelectedRules(prev => prev.filter(id => 
                !typeRules.some(rule => rule.id === id)
            ));
        } else {
            setSelectedRules(prev => [
                ...prev,
                ...typeRules.map(rule => rule.id).filter(id => !prev.includes(id))
            ]);
        }
    };

    const handleUnapplySelected = async () => {
        await onUnapplyRules(selectedRules);
        setSelectedRules([]);
        setIsSelectionMode(false);
    };

    const simpleRules = rules.filter(rule => rule.type === 'simple');
    const conditionalRules = rules.filter(rule => rule.type === 'conditional');

    return (
        <div className="card mb-4">
            <div className="card-header d-flex justify-content-between align-items-center">
                <h3 className="mb-0">적용된 규칙</h3>
                <div className="d-flex gap-2">
                    <button 
                        className="btn btn-outline-primary"
                        onClick={() => setIsSelectionMode(!isSelectionMode)}
                    >
                        {isSelectionMode ? '적용 해제' : '규칙 선택'}
                    </button>
                    {isSelectionMode && selectedRules.length > 0 && (
                        <button 
                            className="btn btn-danger"
                            onClick={handleUnapplySelected}
                        >
                            선택한 규칙 적용 해제 ({selectedRules.length}개)
                        </button>
                    )}
                </div>
            </div>
            
            {/* 단순 응답 규칙 */}
            <div className="card-body">
                <h4 className="mb-3">단순 응답 규칙</h4>
                <div className="table-responsive mb-4">
                    <table className="table table-hover">
                        <thead className="table-light">
                            <tr>
                                {isSelectionMode && (
                                    <th width="50px">
                                        <div className="form-check">
                                            <input
                                                type="checkbox"
                                                className="form-check-input"
                                                checked={simpleRules.every(rule => selectedRules.includes(rule.id))}
                                                onChange={() => handleSelectAllRules('simple')}
                                            />
                                        </div>
                                    </th>
                                )}
                                <th>트리거 단어</th>
                                <th>응답 내용</th>
                            </tr>
                        </thead>
                        <tbody>
                            {simpleRules.map(rule => (
                                <tr key={rule.id}>
                                    {isSelectionMode && (
                                        <td>
                                            <div className="form-check">
                                                <input
                                                    type="checkbox"
                                                    className="form-check-input"
                                                    checked={selectedRules.includes(rule.id)}
                                                    onChange={() => handleSelectRule(rule.id)}
                                                />
                                            </div>
                                        </td>
                                    )}
                                    <td>{rule.triggerWords.join(', ')}</td>
                                    <td>{rule.response}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>

                {/* 조건부 응답 규칙 */}
                <h4 className="mb-3">조건부 응답 규칙</h4>
                <div className="table-responsive">
                    <table className="table table-hover">
                        <thead className="table-light">
                            <tr>
                                {isSelectionMode && (
                                    <th width="50px">
                                        <div className="form-check">
                                            <input
                                                type="checkbox"
                                                className="form-check-input"
                                                checked={conditionalRules.every(rule => selectedRules.includes(rule.id))}
                                                onChange={() => handleSelectAllRules('conditional')}
                                            />
                                        </div>
                                    </th>
                                )}
                                <th>트리거 단어</th>
                                <th>기본 응답</th>
                                <th>조건부 응답</th>
                            </tr>
                        </thead>
                        <tbody>
                            {conditionalRules.map(rule => (
                                <tr key={rule.id}>
                                    {isSelectionMode && (
                                        <td>
                                            <div className="form-check">
                                                <input
                                                    type="checkbox"
                                                    className="form-check-input"
                                                    checked={selectedRules.includes(rule.id)}
                                                    onChange={() => handleSelectRule(rule.id)}
                                                />
                                            </div>
                                        </td>
                                    )}
                                    <td>{rule.triggerWords.join(', ')}</td>
                                    <td>{rule.response}</td>
                                    <td>
                                        <div className="d-flex flex-column gap-2">
                                            {rule.conditions.map((condition, index) => (
                                                <div key={index} className="p-2 bg-light rounded">
                                                    <strong className="text-primary">{condition.conditionText}:</strong>
                                                    <div className="ms-2">{condition.response}</div>
                                                </div>
                                            ))}
                                        </div>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
};

export default AppliedRules; 